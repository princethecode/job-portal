package com.emps.abroadjobs.network;

import android.util.Log;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import retrofit2.Call;

/**
 * Helper class to test Retrofit interface methods for proper generic types
 * This helps identify raw Call types that cause crashes in production
 * Note: getUserProfile() legitimately uses Call<User> due to backend API design
 */
public class RetrofitTestHelper {
    private static final String TAG = "RetrofitTestHelper";
    
    // Methods that legitimately use raw Call types (backend doesn't wrap them)
    private static final String[] ALLOWED_RAW_CALL_METHODS = {
        "getUserProfile"
    };
    
    /**
     * Validates that all methods in ApiService have proper generic Call types
     * Call this in debug builds to catch raw Call types early
     */
    public static void validateApiServiceMethods() {
        Log.d(TAG, "Validating ApiService methods for proper generic types...");
        
        Method[] methods = ApiService.class.getDeclaredMethods();
        boolean hasIssues = false;
        
        for (Method method : methods) {
            Type returnType = method.getGenericReturnType();
            
            if (returnType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) returnType;
                Type rawType = paramType.getRawType();
                
                if (rawType.equals(Call.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length == 0) {
                        if (isAllowedRawCallMethod(method.getName())) {
                            Log.w(TAG, "⚠️ Method " + method.getName() + " has raw Call type (allowed for backend compatibility)");
                        } else {
                            Log.e(TAG, "❌ Method " + method.getName() + " has raw Call type!");
                            hasIssues = true;
                        }
                    } else {
                        Log.d(TAG, "✅ Method " + method.getName() + " has proper generic: " + typeArgs[0]);
                    }
                }
            } else if (returnType.equals(Call.class)) {
                if (isAllowedRawCallMethod(method.getName())) {
                    Log.w(TAG, "⚠️ Method " + method.getName() + " has raw Call type (allowed for backend compatibility)");
                } else {
                    Log.e(TAG, "❌ Method " + method.getName() + " has raw Call type!");
                    hasIssues = true;
                }
            }
        }
        
        if (!hasIssues) {
            Log.d(TAG, "✅ All ApiService methods have proper generic types (or are explicitly allowed)");
        } else {
            Log.e(TAG, "❌ Found methods with raw Call types - these will crash in production!");
        }
    }
    
    private static boolean isAllowedRawCallMethod(String methodName) {
        for (String allowedMethod : ALLOWED_RAW_CALL_METHODS) {
            if (allowedMethod.equals(methodName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validates RecruiterApiService methods
     */
    public static void validateRecruiterApiServiceMethods() {
        Log.d(TAG, "Validating RecruiterApiService methods for proper generic types...");
        
        Method[] methods = RecruiterApiService.class.getDeclaredMethods();
        boolean hasIssues = false;
        
        for (Method method : methods) {
            Type returnType = method.getGenericReturnType();
            
            if (returnType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) returnType;
                Type rawType = paramType.getRawType();
                
                if (rawType.equals(Call.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length == 0) {
                        Log.e(TAG, "❌ RecruiterApiService method " + method.getName() + " has raw Call type!");
                        hasIssues = true;
                    } else {
                        Log.d(TAG, "✅ RecruiterApiService method " + method.getName() + " has proper generic: " + typeArgs[0]);
                    }
                }
            } else if (returnType.equals(Call.class)) {
                Log.e(TAG, "❌ RecruiterApiService method " + method.getName() + " has raw Call type!");
                hasIssues = true;
            }
        }
        
        if (!hasIssues) {
            Log.d(TAG, "✅ All RecruiterApiService methods have proper generic types");
        } else {
            Log.e(TAG, "❌ Found RecruiterApiService methods with raw Call types - these will crash in production!");
        }
    }
}