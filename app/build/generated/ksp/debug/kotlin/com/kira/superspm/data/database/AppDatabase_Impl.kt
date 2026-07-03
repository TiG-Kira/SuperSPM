package com.kira.superspm.`data`.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _locationRecordDao: Lazy<LocationRecordDao> = lazy {
    LocationRecordDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "0bb325dc6b17e96d4831d1bb28f4ba75", "3204136d8499e80c651f4e955eda47f4") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `location_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER, `maxSpeed` REAL NOT NULL, `avgSpeed` REAL NOT NULL, `totalDistance` REAL NOT NULL, `dataPoints` INTEGER NOT NULL, `pathData` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '0bb325dc6b17e96d4831d1bb28f4ba75')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `location_records`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsLocationRecords: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsLocationRecords.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationRecords.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationRecords.put("startTime", TableInfo.Column("startTime", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationRecords.put("endTime", TableInfo.Column("endTime", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationRecords.put("maxSpeed", TableInfo.Column("maxSpeed", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationRecords.put("avgSpeed", TableInfo.Column("avgSpeed", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationRecords.put("totalDistance", TableInfo.Column("totalDistance", "REAL", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationRecords.put("dataPoints", TableInfo.Column("dataPoints", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLocationRecords.put("pathData", TableInfo.Column("pathData", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysLocationRecords: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesLocationRecords: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoLocationRecords: TableInfo = TableInfo("location_records", _columnsLocationRecords,
            _foreignKeysLocationRecords, _indicesLocationRecords)
        val _existingLocationRecords: TableInfo = read(connection, "location_records")
        if (!_infoLocationRecords.equals(_existingLocationRecords)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |location_records(com.kira.superspm.data.model.LocationRecord).
              | Expected:
              |""".trimMargin() + _infoLocationRecords + """
              |
              | Found:
              |""".trimMargin() + _existingLocationRecords)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "location_records")
  }

  public override fun clearAllTables() {
    super.performClear(false, "location_records")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(LocationRecordDao::class, LocationRecordDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun locationRecordDao(): LocationRecordDao = _locationRecordDao.value
}
