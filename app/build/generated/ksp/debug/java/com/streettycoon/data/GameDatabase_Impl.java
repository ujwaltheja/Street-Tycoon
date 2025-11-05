package com.streettycoon.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class GameDatabase_Impl extends GameDatabase {
  private volatile GameSnapshotDao _gameSnapshotDao;

  private volatile PlayerPrefsDao _playerPrefsDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `game_snapshots` (`id` INTEGER NOT NULL, `snapshotJson` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `playerCash` REAL NOT NULL, `playerLevel` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `player_prefs` (`id` INTEGER NOT NULL, `soundEnabled` INTEGER NOT NULL, `musicEnabled` INTEGER NOT NULL, `language` TEXT NOT NULL, `lastPlayedTimestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'bf186973c97e92b856b02e9239352778')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `game_snapshots`");
        db.execSQL("DROP TABLE IF EXISTS `player_prefs`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsGameSnapshots = new HashMap<String, TableInfo.Column>(5);
        _columnsGameSnapshots.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSnapshots.put("snapshotJson", new TableInfo.Column("snapshotJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSnapshots.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSnapshots.put("playerCash", new TableInfo.Column("playerCash", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGameSnapshots.put("playerLevel", new TableInfo.Column("playerLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGameSnapshots = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGameSnapshots = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGameSnapshots = new TableInfo("game_snapshots", _columnsGameSnapshots, _foreignKeysGameSnapshots, _indicesGameSnapshots);
        final TableInfo _existingGameSnapshots = TableInfo.read(db, "game_snapshots");
        if (!_infoGameSnapshots.equals(_existingGameSnapshots)) {
          return new RoomOpenHelper.ValidationResult(false, "game_snapshots(com.streettycoon.data.GameSnapshotEntity).\n"
                  + " Expected:\n" + _infoGameSnapshots + "\n"
                  + " Found:\n" + _existingGameSnapshots);
        }
        final HashMap<String, TableInfo.Column> _columnsPlayerPrefs = new HashMap<String, TableInfo.Column>(5);
        _columnsPlayerPrefs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerPrefs.put("soundEnabled", new TableInfo.Column("soundEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerPrefs.put("musicEnabled", new TableInfo.Column("musicEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerPrefs.put("language", new TableInfo.Column("language", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerPrefs.put("lastPlayedTimestamp", new TableInfo.Column("lastPlayedTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlayerPrefs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlayerPrefs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlayerPrefs = new TableInfo("player_prefs", _columnsPlayerPrefs, _foreignKeysPlayerPrefs, _indicesPlayerPrefs);
        final TableInfo _existingPlayerPrefs = TableInfo.read(db, "player_prefs");
        if (!_infoPlayerPrefs.equals(_existingPlayerPrefs)) {
          return new RoomOpenHelper.ValidationResult(false, "player_prefs(com.streettycoon.data.PlayerPrefsEntity).\n"
                  + " Expected:\n" + _infoPlayerPrefs + "\n"
                  + " Found:\n" + _existingPlayerPrefs);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "bf186973c97e92b856b02e9239352778", "da28760badea06d73eebf4bb40d4d84a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "game_snapshots","player_prefs");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `game_snapshots`");
      _db.execSQL("DELETE FROM `player_prefs`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(GameSnapshotDao.class, GameSnapshotDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PlayerPrefsDao.class, PlayerPrefsDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public GameSnapshotDao gameSnapshotDao() {
    if (_gameSnapshotDao != null) {
      return _gameSnapshotDao;
    } else {
      synchronized(this) {
        if(_gameSnapshotDao == null) {
          _gameSnapshotDao = new GameSnapshotDao_Impl(this);
        }
        return _gameSnapshotDao;
      }
    }
  }

  @Override
  public PlayerPrefsDao playerPrefsDao() {
    if (_playerPrefsDao != null) {
      return _playerPrefsDao;
    } else {
      synchronized(this) {
        if(_playerPrefsDao == null) {
          _playerPrefsDao = new PlayerPrefsDao_Impl(this);
        }
        return _playerPrefsDao;
      }
    }
  }
}
