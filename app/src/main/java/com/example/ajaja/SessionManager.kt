package com.example.ajaja

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

// 한 계정의 데이터 구조
data class UserProfile(
    val userId: String,
    val userPw: String,
    val name: String,
    val level: String,     // 초등/중등/고등 (옵션)
    val grade: String,     // "중1" 형태
    val subject: String,   // "C언어" 등
    val enrollAt: Long     // 최초 가입 시각 (UTC millis)
)

object SessionManager {
    private const val PREF = "user_prefs"

    private const val KEY_USERS_JSON = "users_json"           // 모든 계정 저장 (JSON Array)
    private const val KEY_CURRENT_USER_ID = "current_user_id" // 현재 로그인한 계정

    private fun sp(ctx: Context) = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    // ---------- JSON <-> 객체 변환 ----------
    private fun UserProfile.toJson(): JSONObject = JSONObject().apply {
        put("userId", userId)
        put("userPw", userPw)
        put("name", name)
        put("level", level)
        put("grade", grade)
        put("subject", subject)
        put("enrollAt", enrollAt)
    }

    private fun fromJson(o: JSONObject) = UserProfile(
        userId = o.optString("userId"),
        userPw = o.optString("userPw"),
        name = o.optString("name"),
        level = o.optString("level"),
        grade = o.optString("grade"),
        subject = o.optString("subject"),
        enrollAt = o.optLong("enrollAt", System.currentTimeMillis())
    )

    private fun readUsers(ctx: Context): MutableList<UserProfile> {
        val raw = sp(ctx).getString(KEY_USERS_JSON, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = mutableListOf<UserProfile>()
        for (i in 0 until arr.length()) {
            list += fromJson(arr.getJSONObject(i))
        }
        return list
    }

    private fun writeUsers(ctx: Context, list: List<UserProfile>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        sp(ctx).edit { putString(KEY_USERS_JSON, arr.toString()) }
    }

    // ---------- 공개 API ----------
    /** 아이디가 이미 존재하는지 */
    fun isUserIdTaken(ctx: Context, userId: String): Boolean =
        readUsers(ctx).any { it.userId == userId }

    /** 새 사용자 추가 (성공 true / 중복 false) */
    fun addUser(ctx: Context, profile: UserProfile): Boolean {
        val users = readUsers(ctx)
        if (users.any { it.userId == profile.userId }) return false
        users += profile
        writeUsers(ctx, users)
        return true
    }

    /** 특정 사용자 조회 */
    fun getUser(ctx: Context, userId: String): UserProfile? =
        readUsers(ctx).firstOrNull { it.userId == userId }

    /** 모든 사용자 */
    fun getAllUsers(ctx: Context): List<UserProfile> = readUsers(ctx)

    /** 로그인 성공 시 현재 사용자 설정 */
    fun setCurrentUser(ctx: Context, userId: String) {
        sp(ctx).edit { putString(KEY_CURRENT_USER_ID, userId) }
    }

    /** 현재 로그인한 사용자 */
    fun getCurrentUser(ctx: Context): UserProfile? {
        val id = sp(ctx).getString(KEY_CURRENT_USER_ID, null) ?: return null
        return getUser(ctx, id)
    }

    /** 로그아웃(현재 사용자만 해제; 계정 목록은 보존) */
    fun logout(ctx: Context) {
        sp(ctx).edit { remove(KEY_CURRENT_USER_ID) }
    }

    /** 전부 삭제(테스트/초기화용) */
    fun clearAll(ctx: Context) {
        sp(ctx).edit { clear() }
    }
}
