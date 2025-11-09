package com.snipper.model;

/**
 * Enum representing the visibility levels for code snippets
 */
public enum VisibilityType {
    /**
     * Snippet is visible to everyone, including anonymous users
     */
    PUBLIC("public"),
    
    /**
     * Snippet is only visible to the owner
     */
    PRIVATE("private"),
    
    /**
     * Snippet is accessible via direct link but not listed publicly
     */
    UNLISTED("unlisted");

    private final String value;

    VisibilityType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get VisibilityType from string value
     * @param value the string value
     * @return corresponding VisibilityType
     * @throws IllegalArgumentException if value is not valid
     */
    public static VisibilityType fromValue(String value) {
        for (VisibilityType type : VisibilityType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid visibility type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}