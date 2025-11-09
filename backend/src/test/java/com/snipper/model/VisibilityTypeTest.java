package com.snipper.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VisibilityTypeTest {

    @Test
    void testVisibilityTypeValues() {
        assertEquals("public", VisibilityType.PUBLIC.getValue());
        assertEquals("private", VisibilityType.PRIVATE.getValue());
        assertEquals("unlisted", VisibilityType.UNLISTED.getValue());
    }

    @Test
    void testFromValue() {
        assertEquals(VisibilityType.PUBLIC, VisibilityType.fromValue("public"));
        assertEquals(VisibilityType.PRIVATE, VisibilityType.fromValue("private"));
        assertEquals(VisibilityType.UNLISTED, VisibilityType.fromValue("unlisted"));
        
        // Test case insensitive
        assertEquals(VisibilityType.PUBLIC, VisibilityType.fromValue("PUBLIC"));
        assertEquals(VisibilityType.PRIVATE, VisibilityType.fromValue("Private"));
        assertEquals(VisibilityType.UNLISTED, VisibilityType.fromValue("UNLISTED"));
    }

    @Test
    void testFromValueInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            VisibilityType.fromValue("invalid");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            VisibilityType.fromValue("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            VisibilityType.fromValue(null);
        });
    }

    @Test
    void testToString() {
        assertEquals("public", VisibilityType.PUBLIC.toString());
        assertEquals("private", VisibilityType.PRIVATE.toString());
        assertEquals("unlisted", VisibilityType.UNLISTED.toString());
    }

    @Test
    void testEnumValues() {
        VisibilityType[] values = VisibilityType.values();
        assertEquals(3, values.length);
        
        assertTrue(java.util.Arrays.asList(values).contains(VisibilityType.PUBLIC));
        assertTrue(java.util.Arrays.asList(values).contains(VisibilityType.PRIVATE));
        assertTrue(java.util.Arrays.asList(values).contains(VisibilityType.UNLISTED));
    }
}