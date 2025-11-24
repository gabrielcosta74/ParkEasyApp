package pt.ubi.pdm.parkeasyapp.api.models;

public class ParkingSession {
    public String id;
    public Double lat;
    public Double lng;
    public String created_at;
    public String expires_at; // ISO8601
    public Integer reminder_offset_minutes;
    public String photo_path;
    public String notes;
}