package com.agridirect.category;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active")
    private boolean isActive = true;

    public Category() {}

    private Category(Builder b) {
        this.name = b.name;
        this.imageUrl = b.imageUrl;
        this.isActive = b.isActive;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name, imageUrl;
        private boolean isActive = true;
        public Builder name(String v)     { this.name = v; return this; }
        public Builder imageUrl(String v) { this.imageUrl = v; return this; }
        public Builder isActive(boolean v){ this.isActive = v; return this; }
        public Category build()           { return new Category(this); }
    }

    public UUID getId()        { return id; }
    public String getName()    { return name; }
    public String getImageUrl(){ return imageUrl; }
    public boolean isActive()  { return isActive; }

    public void setId(UUID id)          { this.id = id; }
    public void setName(String v)       { this.name = v; }
    public void setImageUrl(String v)   { this.imageUrl = v; }
    public void setActive(boolean v)    { this.isActive = v; }
}
