package pt.ubi.pdm.parkeasyapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocalCache {
    private final SharedPreferences sp;

    public LocalCache(Context ctx) {
        sp = ctx.getSharedPreferences("parkeasy", Context.MODE_PRIVATE);
    }

    // Guarda um draft de sessão offline (em JSON)
    public void saveSessionDraft(JSONObject obj) {
        try {
            JSONArray arr = new JSONArray(sp.getString("drafts", "[]"));
            arr.put(obj);
            sp.edit().putString("drafts", arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Recupera todos os drafts
    public JSONArray getDrafts() {
        try {
            return new JSONArray(sp.getString("drafts", "[]"));
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    // Limpa apenas os drafts
    public void clearDrafts() {
        sp.edit().remove("drafts").apply();
    }

    // Guarda tokens Supabase
    public void setTokens(String access, String refresh) {
        sp.edit().putString("access", access).putString("refresh", refresh).apply();
    }

    public String getAccess() {
        return sp.getString("access", null);
    }

    public String getRefresh() {
        return sp.getString("refresh", null);
    }

    // ✅ Guarda e lê o ID do utilizador autenticado
    public void setUserId(String userId) {
        sp.edit().putString("user_id", userId).apply();
    }

    public String getUserId() {
        return sp.getString("user_id", "me");
    }

    // ✅ Limpa tudo (logout)
    public void clear() {
        sp.edit().clear().apply();
    }
}
