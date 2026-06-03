package com.agridirect.auth.dto;

public class RegisterRequest {

    private String idToken;
    private String name;
    private String role;
    private String email;

    // Farmer fields
    private String farmName;
    private String location;
    private Double landAcres;

    // Buyer fields
    private String buyerType;
    private String address;
    private String gstNumber;

    // Delivery fields
    private String vehicleType;
    private String licenseNo;

    public RegisterRequest() {}

    public String getIdToken()      { return idToken; }
    public String getName()         { return name; }
    public String getRole()         { return role; }
    public String getEmail()        { return email; }
    public String getFarmName()     { return farmName; }
    public String getLocation()     { return location; }
    public Double getLandAcres()    { return landAcres; }
    public String getBuyerType()    { return buyerType; }
    public String getAddress()      { return address; }
    public String getGstNumber()    { return gstNumber; }
    public String getVehicleType()  { return vehicleType; }
    public String getLicenseNo()    { return licenseNo; }

    public void setIdToken(String v)     { this.idToken = v; }
    public void setName(String v)        { this.name = v; }
    public void setRole(String v)        { this.role = v; }
    public void setEmail(String v)       { this.email = v; }
    public void setFarmName(String v)    { this.farmName = v; }
    public void setLocation(String v)    { this.location = v; }
    public void setLandAcres(Double v)   { this.landAcres = v; }
    public void setBuyerType(String v)   { this.buyerType = v; }
    public void setAddress(String v)     { this.address = v; }
    public void setGstNumber(String v)   { this.gstNumber = v; }
    public void setVehicleType(String v) { this.vehicleType = v; }
    public void setLicenseNo(String v)   { this.licenseNo = v; }
}
