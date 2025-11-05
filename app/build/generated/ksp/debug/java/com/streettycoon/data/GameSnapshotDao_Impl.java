package com.streettycoon.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class GameSnapshotDao_Impl implements GameSnapshotDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GameSnapshotEntity> __insertionAdapterOfGameSnapshotEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public GameSnapshotDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGameSnapshotEntity = new EntityInsertionAdapter<GameSnapshotEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `game_snapshots` (`id`,`snapshotJson`,`timestamp`,`playerCash`,`playerLevel`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GameSnapshotEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSnapshotJson());
        statement.bindLong(3, entity.getTimestamp());
        statement.bindDouble(4, entity.getPlayerCash());
        statement.bindLong(5, entity.getPlayerLevel());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM game_snapshots";
        return _query;
      }
    };
  }

  @Override
  public Object saveSnapshot(final GameSnapshotEntity snapshot,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfGameSnapshotEntity.insert(snapshot);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getSnapshot(final Continuation<? super GameSnapshotEntity> $completion) {
    final String _sql = "SELECT * FROM game_snapshots WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<GameSnapshotEntity>() {
      @Override
      @Nullable
      public GameSnapshotEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSnapshotJson = CursorUtil.getColumnIndexOrThrow(_cursor, "snapshotJson");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfPlayerCash = CursorUtil.getColumnIndexOrThrow(_cursor, "playerCash");
          final int _cursorIndexOfPlayerLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "playerLevel");
          final GameSnapshotEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpSnapshotJson;
            _tmpSnapshotJson = _cursor.getString(_cursorIndexOfSnapshotJson);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final double _tmpPlayerCash;
            _tmpPlayerCash = _cursor.getDouble(_cursorIndexOfPlayerCash);
            final int _tmpPlayerLevel;
            _tmpPlayerLevel = _cursor.getInt(_cursorIndexOfPlayerLevel);
            _result = new GameSnapshotEntity(_tmpId,_tmpSnapshotJson,_tmpTimestamp,_tmpPlayerCash,_tmpPlayerLevel);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
