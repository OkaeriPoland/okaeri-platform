package org.example.okaeriplatformtest.persistence;

import eu.okaeri.persistence.document.Document;
import eu.okaeri.platform.web.meta.role.SimpleRouteRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
// custom access store object for persistence
// remember to use SerdesWeb or register your
// own transformer/serializer for SimpleRouteRole
public class Access extends Document {
    private String token;
    private Set<SimpleRouteRole> roles = new HashSet<>();
}