package com.kira.superspm.utils

import android.location.Location
import com.kira.superspm.data.model.LocationPoint

/**
 * GPS 里程跟踪器 —— 测速里程的唯一计算入口。
 *
 * 旧实现的问题：
 * 1. GPS 与 NETWORK 两个 provider 同时往同一个累加器喂点，两者定位结果互跳时
 *    产生大量"幽灵里程"（网络定位精度常在数百米，一跳就是几百米）；
 * 2. 无定位精度过滤，室内 / 窗边的网络定位点直接计入里程与轨迹；
 * 3. 无抖动 / 跳变过滤：原地不动时 GPS 位置抖动（±数米）持续累积里程，
 *    provider 切换、定位恢复时的一次远距离跳点也被全额计入；
 * 4. UI 与服务各有一套累加器且采样率不同，屏幕显示里程与存库里程不是同一个数。
 *
 * 过滤策略（按顺序）：
 * - 精度门限：[maxAccuracyM] 之外的点整体丢弃，不进里程也不进轨迹；
 * - 时间门限：相邻点间隔 ≥ [maxGapMs] 的段丢弃（定位断续，中途不可信）；
 * - 跳变门限：段内隐含速度 > [maxImpliedSpeedMps] 的段丢弃（位置瞬移）；
 * - 抖动门限：多普勒速度低于 [movingSpeedMps]（设备判定为静止）时，
 *   段长短于 max([noiseFloorM], 2 × 平均定位精度) 的段判定为静止抖动，丢弃；
 *   多普勒速度达标（真实移动）时段长再短也保留，慢走蠕行不掉数据。
 *
 * 距离计算使用平台原生 [Location.distanceBetween]（较手写 Haversine 更快更稳）。
 * 段被丢弃时基准点仍会更新，避免单个坏点污染后续所有段。
 */
class MileageTracker(
    /** 精度门限（米）：差于此值的定位点整体丢弃 */
    var maxAccuracyM: Float = DEFAULT_MAX_ACCURACY_M,
    /** 抖动地板（米）：静止时的最短可信段长 */
    var noiseFloorM: Float = DEFAULT_NOISE_FLOOR_M,
    /** 移动门限（米/秒）：多普勒速度高于此值视为真实移动 */
    var movingSpeedMps: Float = DEFAULT_MOVING_SPEED_MPS,
    /** 跳变门限（米/秒）：段内隐含速度高于此值视为定位瞬移 */
    var maxImpliedSpeedMps: Float = DEFAULT_MAX_IMPLIED_SPEED_MPS,
    /** 时间门限（毫秒）：相邻点间隔超过此值的段丢弃 */
    var maxGapMs: Long = DEFAULT_MAX_GAP_MS
) {
    private var lastPoint: LocationPoint? = null
    private var lastAccuracyM: Float = Float.MAX_VALUE

    /** 当前累计里程（公里） */
    var totalDistanceKm: Double = 0.0
        private set

    /** 被采纳的轨迹点数 */
    var acceptedPoints: Int = 0
        private set

    /** 因精度不合格被丢弃的点数 */
    var rejectedPoints: Int = 0
        private set

    fun reset() {
        lastPoint = null
        lastAccuracyM = Float.MAX_VALUE
        totalDistanceKm = 0.0
        acceptedPoints = 0
        rejectedPoints = 0
    }

    /**
     * 喂入一个定位点。
     *
     * @param latitude 纬度
     * @param longitude 经度
     * @param accuracyM 定位精度（米）；0 表示设备未提供，按不合格处理之外的保守值参与抖动计算
     * @param speedMps GPS 多普勒速度（米/秒），用于区分真实移动与静止抖动
     * @param timestampMs 定位时间戳（毫秒）
     * @return 被采纳时返回轨迹点（调用方加入 pathPoints）；精度不合格时返回 null
     */
    fun add(
        latitude: Double,
        longitude: Double,
        accuracyM: Float,
        speedMps: Float,
        timestampMs: Long
    ): LocationPoint? {
        if (accuracyM < 0f || accuracyM > maxAccuracyM) {
            rejectedPoints++
            return null
        }

        val point = LocationPoint(
            latitude = latitude,
            longitude = longitude,
            speed = speedMps * 3.6,
            timestamp = timestampMs
        )

        val last = lastPoint
        if (last != null) {
            val dtMs = timestampMs - last.timestamp
            // dtMs <= 0：乱序 / 重复 fix，只更新基准点不计里程
            if (dtMs in 1 until maxGapMs) {
                val results = FloatArray(1)
                Location.distanceBetween(last.latitude, last.longitude, latitude, longitude, results)
                val segmentM = results[0].toDouble()
                if (segmentM > 0.0) {
                    val impliedSpeedMps = segmentM / (dtMs / 1000.0)
                    val avgAccuracyM = if (accuracyM > 0f && lastAccuracyM.isFinite()) {
                        (accuracyM + lastAccuracyM) / 2f
                    } else {
                        maxAccuracyM
                    }
                    val noiseFloor = maxOf(noiseFloorM.toDouble(), avgAccuracyM * 2.0)
                    val isMoving = speedMps >= movingSpeedMps
                    val isJitter = !isMoving && segmentM < noiseFloor
                    val isTeleport = impliedSpeedMps > maxImpliedSpeedMps
                    if (!isJitter && !isTeleport) {
                        totalDistanceKm += segmentM / 1000.0
                    }
                }
            }
        }

        lastPoint = point
        lastAccuracyM = if (accuracyM > 0f) accuracyM else maxAccuracyM
        acceptedPoints++
        return point
    }

    /**
     * 传感器（惯性推算）里程注入：无定位点可过滤，直接累加。
     * 与 [add] 互斥使用——同一时段只应有一种模式在喂数据。
     */
    fun addDistance(deltaKm: Double) {
        if (deltaKm > 0.0) {
            totalDistanceKm += deltaKm
        }
    }

    companion object {
        /** 默认精度门限：50 米（室内网络定位普遍差于此值，室外 GPS 通常 3-20 米） */
        const val DEFAULT_MAX_ACCURACY_M = 50f

        /** 默认抖动地板：3 米 */
        const val DEFAULT_NOISE_FLOOR_M = 3f

        /** 默认移动门限：0.5 米/秒 ≈ 1.8 km/h（低于此按静止处理，防抖动） */
        const val DEFAULT_MOVING_SPEED_MPS = 0.5f

        /** 默认跳变门限：83.3 米/秒 = 300 km/h（民航客机巡航都不够，命中即瞬移） */
        const val DEFAULT_MAX_IMPLIED_SPEED_MPS = 83.3f

        /** 默认时间门限：60 秒（超过视为定位断续） */
        const val DEFAULT_MAX_GAP_MS = 60_000L
    }
}
