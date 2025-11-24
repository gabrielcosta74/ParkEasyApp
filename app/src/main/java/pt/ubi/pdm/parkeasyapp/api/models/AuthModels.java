package pt.ubi.pdm.parkeasyapp.api.models;

public class AuthModels {
    public static class LoginRequest {
        public String email;
        public String password;
        public String gotrue_meta_security; // placeholder para compat
        public LoginRequest(String email, String password) {
            this.email = email; this.password = password;
        }
    }
    public static class SignupRequest {
        public String email; public String password;
        public SignupRequest(String e, String p){email=e;password=p;}
    }
    public static class SessionResponse {
        public String access_token;
        public String token_type;
        public String refresh_token;
        public String expires_in;
        public User user;
        public static class User { public String id; public String email; }
    }
}