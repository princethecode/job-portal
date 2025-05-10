package com.example.jobportal.data.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.jobportal.models.User;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserDao_Impl implements UserDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<User> __insertionAdapterOfUser;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllUsers;

  public UserDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUser = new EntityInsertionAdapter<User>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `users` (`id`,`fullName`,`email`,`phone`,`skills`,`experience`,`resume`,`isActive`,`isAdmin`,`emailVerifiedAt`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final User entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getFullName());
        statement.bindString(3, entity.getEmail());
        statement.bindString(4, entity.getPhone());
        statement.bindString(5, entity.getSkills());
        statement.bindString(6, entity.getExperience());
        statement.bindString(7, entity.getResume());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(8, _tmp);
        final int _tmp_1 = entity.isAdmin() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        statement.bindString(10, entity.getEmailVerifiedAt());
        statement.bindString(11, entity.getCreatedAt());
        statement.bindString(12, entity.getUpdatedAt());
      }
    };
    this.__preparedStmtOfDeleteAllUsers = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM users";
        return _query;
      }
    };
  }

  @Override
  public void insert(final User user) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfUser.insert(user);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAllUsers() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllUsers.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAllUsers.release(_stmt);
    }
  }

  @Override
  public LiveData<User> getUserById(final String userId) {
    final String _sql = "SELECT * FROM users WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"users"}, false, new Callable<User>() {
      @Override
      @Nullable
      public User call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfSkills = CursorUtil.getColumnIndexOrThrow(_cursor, "skills");
          final int _cursorIndexOfExperience = CursorUtil.getColumnIndexOrThrow(_cursor, "experience");
          final int _cursorIndexOfResume = CursorUtil.getColumnIndexOrThrow(_cursor, "resume");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsAdmin = CursorUtil.getColumnIndexOrThrow(_cursor, "isAdmin");
          final int _cursorIndexOfEmailVerifiedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "emailVerifiedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final User _result;
          if (_cursor.moveToFirst()) {
            _result = new User();
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            _result.setId(_tmpId);
            final String _tmpFullName;
            _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            _result.setFullName(_tmpFullName);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            _result.setEmail(_tmpEmail);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            _result.setPhone(_tmpPhone);
            final String _tmpSkills;
            _tmpSkills = _cursor.getString(_cursorIndexOfSkills);
            _result.setSkills(_tmpSkills);
            final String _tmpExperience;
            _tmpExperience = _cursor.getString(_cursorIndexOfExperience);
            _result.setExperience(_tmpExperience);
            final String _tmpResume;
            _tmpResume = _cursor.getString(_cursorIndexOfResume);
            _result.setResume(_tmpResume);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _result.setActive(_tmpIsActive);
            final boolean _tmpIsAdmin;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsAdmin);
            _tmpIsAdmin = _tmp_1 != 0;
            _result.setAdmin(_tmpIsAdmin);
            final String _tmpEmailVerifiedAt;
            _tmpEmailVerifiedAt = _cursor.getString(_cursorIndexOfEmailVerifiedAt);
            _result.setEmailVerifiedAt(_tmpEmailVerifiedAt);
            final String _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            _result.setCreatedAt(_tmpCreatedAt);
            final String _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            _result.setUpdatedAt(_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<User> getUserByEmail(final String email) {
    final String _sql = "SELECT * FROM users WHERE email = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, email);
    return __db.getInvalidationTracker().createLiveData(new String[] {"users"}, false, new Callable<User>() {
      @Override
      @Nullable
      public User call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfSkills = CursorUtil.getColumnIndexOrThrow(_cursor, "skills");
          final int _cursorIndexOfExperience = CursorUtil.getColumnIndexOrThrow(_cursor, "experience");
          final int _cursorIndexOfResume = CursorUtil.getColumnIndexOrThrow(_cursor, "resume");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfIsAdmin = CursorUtil.getColumnIndexOrThrow(_cursor, "isAdmin");
          final int _cursorIndexOfEmailVerifiedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "emailVerifiedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final User _result;
          if (_cursor.moveToFirst()) {
            _result = new User();
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            _result.setId(_tmpId);
            final String _tmpFullName;
            _tmpFullName = _cursor.getString(_cursorIndexOfFullName);
            _result.setFullName(_tmpFullName);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            _result.setEmail(_tmpEmail);
            final String _tmpPhone;
            _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            _result.setPhone(_tmpPhone);
            final String _tmpSkills;
            _tmpSkills = _cursor.getString(_cursorIndexOfSkills);
            _result.setSkills(_tmpSkills);
            final String _tmpExperience;
            _tmpExperience = _cursor.getString(_cursorIndexOfExperience);
            _result.setExperience(_tmpExperience);
            final String _tmpResume;
            _tmpResume = _cursor.getString(_cursorIndexOfResume);
            _result.setResume(_tmpResume);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _result.setActive(_tmpIsActive);
            final boolean _tmpIsAdmin;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsAdmin);
            _tmpIsAdmin = _tmp_1 != 0;
            _result.setAdmin(_tmpIsAdmin);
            final String _tmpEmailVerifiedAt;
            _tmpEmailVerifiedAt = _cursor.getString(_cursorIndexOfEmailVerifiedAt);
            _result.setEmailVerifiedAt(_tmpEmailVerifiedAt);
            final String _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            _result.setCreatedAt(_tmpCreatedAt);
            final String _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            _result.setUpdatedAt(_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
