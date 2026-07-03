package com.kira.superspm.`data`.database

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.kira.superspm.`data`.model.Converters
import com.kira.superspm.`data`.model.LocationRecord
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class LocationRecordDao_Impl(
  __db: RoomDatabase,
) : LocationRecordDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfLocationRecord: EntityInsertAdapter<LocationRecord>

  private val __converters: Converters = Converters()

  private val __deleteAdapterOfLocationRecord: EntityDeleteOrUpdateAdapter<LocationRecord>

  private val __updateAdapterOfLocationRecord: EntityDeleteOrUpdateAdapter<LocationRecord>
  init {
    this.__db = __db
    this.__insertAdapterOfLocationRecord = object : EntityInsertAdapter<LocationRecord>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `location_records` (`id`,`name`,`startTime`,`endTime`,`maxSpeed`,`avgSpeed`,`totalDistance`,`dataPoints`,`pathData`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: LocationRecord) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmp: Long? = __converters.dateToTimestamp(entity.startTime)
        if (_tmp == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmp)
        }
        val _tmpEndTime: Instant? = entity.endTime
        val _tmp_1: Long? = __converters.dateToTimestamp(_tmpEndTime)
        if (_tmp_1 == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmp_1)
        }
        statement.bindDouble(5, entity.maxSpeed)
        statement.bindDouble(6, entity.avgSpeed)
        statement.bindDouble(7, entity.totalDistance)
        statement.bindLong(8, entity.dataPoints.toLong())
        statement.bindText(9, entity.pathData)
      }
    }
    this.__deleteAdapterOfLocationRecord = object : EntityDeleteOrUpdateAdapter<LocationRecord>() {
      protected override fun createQuery(): String = "DELETE FROM `location_records` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: LocationRecord) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfLocationRecord = object : EntityDeleteOrUpdateAdapter<LocationRecord>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `location_records` SET `id` = ?,`name` = ?,`startTime` = ?,`endTime` = ?,`maxSpeed` = ?,`avgSpeed` = ?,`totalDistance` = ?,`dataPoints` = ?,`pathData` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: LocationRecord) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmp: Long? = __converters.dateToTimestamp(entity.startTime)
        if (_tmp == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmp)
        }
        val _tmpEndTime: Instant? = entity.endTime
        val _tmp_1: Long? = __converters.dateToTimestamp(_tmpEndTime)
        if (_tmp_1 == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmp_1)
        }
        statement.bindDouble(5, entity.maxSpeed)
        statement.bindDouble(6, entity.avgSpeed)
        statement.bindDouble(7, entity.totalDistance)
        statement.bindLong(8, entity.dataPoints.toLong())
        statement.bindText(9, entity.pathData)
        statement.bindLong(10, entity.id)
      }
    }
  }

  public override suspend fun insertRecord(record: LocationRecord): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfLocationRecord.insert(_connection, record)
  }

  public override suspend fun deleteRecord(record: LocationRecord): Unit = performSuspending(__db,
      false, true) { _connection ->
    __deleteAdapterOfLocationRecord.handle(_connection, record)
  }

  public override suspend fun updateRecord(record: LocationRecord): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfLocationRecord.handle(_connection, record)
  }

  public override fun getAllRecords(): Flow<List<LocationRecord>> {
    val _sql: String = "SELECT * FROM location_records ORDER BY startTime DESC"
    return createFlow(__db, false, arrayOf("location_records")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfMaxSpeed: Int = getColumnIndexOrThrow(_stmt, "maxSpeed")
        val _columnIndexOfAvgSpeed: Int = getColumnIndexOrThrow(_stmt, "avgSpeed")
        val _columnIndexOfTotalDistance: Int = getColumnIndexOrThrow(_stmt, "totalDistance")
        val _columnIndexOfDataPoints: Int = getColumnIndexOrThrow(_stmt, "dataPoints")
        val _columnIndexOfPathData: Int = getColumnIndexOrThrow(_stmt, "pathData")
        val _result: MutableList<LocationRecord> = mutableListOf()
        while (_stmt.step()) {
          val _item: LocationRecord
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpStartTime: Instant
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfStartTime)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfStartTime)
          }
          val _tmp_1: Instant? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpStartTime = _tmp_1
          }
          val _tmpEndTime: Instant?
          val _tmp_2: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getLong(_columnIndexOfEndTime)
          }
          _tmpEndTime = __converters.fromTimestamp(_tmp_2)
          val _tmpMaxSpeed: Double
          _tmpMaxSpeed = _stmt.getDouble(_columnIndexOfMaxSpeed)
          val _tmpAvgSpeed: Double
          _tmpAvgSpeed = _stmt.getDouble(_columnIndexOfAvgSpeed)
          val _tmpTotalDistance: Double
          _tmpTotalDistance = _stmt.getDouble(_columnIndexOfTotalDistance)
          val _tmpDataPoints: Int
          _tmpDataPoints = _stmt.getLong(_columnIndexOfDataPoints).toInt()
          val _tmpPathData: String
          _tmpPathData = _stmt.getText(_columnIndexOfPathData)
          _item =
              LocationRecord(_tmpId,_tmpName,_tmpStartTime,_tmpEndTime,_tmpMaxSpeed,_tmpAvgSpeed,_tmpTotalDistance,_tmpDataPoints,_tmpPathData)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRecordById(id: Long): LocationRecord? {
    val _sql: String = "SELECT * FROM location_records WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfStartTime: Int = getColumnIndexOrThrow(_stmt, "startTime")
        val _columnIndexOfEndTime: Int = getColumnIndexOrThrow(_stmt, "endTime")
        val _columnIndexOfMaxSpeed: Int = getColumnIndexOrThrow(_stmt, "maxSpeed")
        val _columnIndexOfAvgSpeed: Int = getColumnIndexOrThrow(_stmt, "avgSpeed")
        val _columnIndexOfTotalDistance: Int = getColumnIndexOrThrow(_stmt, "totalDistance")
        val _columnIndexOfDataPoints: Int = getColumnIndexOrThrow(_stmt, "dataPoints")
        val _columnIndexOfPathData: Int = getColumnIndexOrThrow(_stmt, "pathData")
        val _result: LocationRecord?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpStartTime: Instant
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfStartTime)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfStartTime)
          }
          val _tmp_1: Instant? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpStartTime = _tmp_1
          }
          val _tmpEndTime: Instant?
          val _tmp_2: Long?
          if (_stmt.isNull(_columnIndexOfEndTime)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getLong(_columnIndexOfEndTime)
          }
          _tmpEndTime = __converters.fromTimestamp(_tmp_2)
          val _tmpMaxSpeed: Double
          _tmpMaxSpeed = _stmt.getDouble(_columnIndexOfMaxSpeed)
          val _tmpAvgSpeed: Double
          _tmpAvgSpeed = _stmt.getDouble(_columnIndexOfAvgSpeed)
          val _tmpTotalDistance: Double
          _tmpTotalDistance = _stmt.getDouble(_columnIndexOfTotalDistance)
          val _tmpDataPoints: Int
          _tmpDataPoints = _stmt.getLong(_columnIndexOfDataPoints).toInt()
          val _tmpPathData: String
          _tmpPathData = _stmt.getText(_columnIndexOfPathData)
          _result =
              LocationRecord(_tmpId,_tmpName,_tmpStartTime,_tmpEndTime,_tmpMaxSpeed,_tmpAvgSpeed,_tmpTotalDistance,_tmpDataPoints,_tmpPathData)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
