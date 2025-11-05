package com.streettycoon.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
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
public final class PlayerPrefsDao_Impl implements PlayerPrefsDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlayerPrefsEntity> __insertionAdapterOfPlayerPrefsEntity;

  public PlayerPrefsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlayerPrefsEntity = new EntityInsertionAdapter<PlayerPrefsEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `player_prefs` (`id`,`soundEnabled`,`musicEnabled`,`language`,`lastPlayedTimestamp`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlayerPrefsEntity entity) {
        statement.bindLong(1, entity.getId());
        final int _tmp = entity.getSoundEnabled() ? 1 : 0;
        statement.bindLong(2, _tmp);
        final int _tmp_1 = entity.getMusicEnabled() ? 1 : 0;
        statement.bindLong(3, _tmp_1);
        statement.bindString(4, entity.getLanguage());
        statement.bindLong(5, entity.getLastPlayedTimestamp());
      }
    };
  }

  @Override
  public Object savePrefs(final PlayerPrefsEntity prefs,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlayerPrefsEntity.insert(prefs);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPrefs(final Continuation<? super PlayerPrefsEntity> $completion) {
    final String _sql = "SELECT * FROM player_prefs WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PlayerPrefsEntity>() {
      @Override
      @Nullable
      public PlayerPrefsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSoundEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "soundEnabled");
          final int _cursorIndexOfMusicEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "musicEnabled");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfLastPlayedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPlayedTimestamp");
          final PlayerPrefsEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final boolean _tmpSoundEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSoundEnabled);
            _tmpSoundEnabled = _tmp != 0;
            final boolean _tmpMusicEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMusicEnabled);
            _tmpMusicEnabled = _tmp_1 != 0;
            final String _tmpLanguage;
            _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            final long _tmpLastPlayedTimestamp;
            _tmpLastPlayedTimestamp = _cursor.getLong(_cursorIndexOfLastPlayedTimestamp);
            _result = new PlayerPrefsEntity(_tmpId,_tmpSoundEnabled,_tmpMusicEnabled,_tmpLanguage,_tmpLastPlayedTimestamp);
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
