package org.apereo.cas.adaptors.duo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.BooleanUtils;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link DuoSecurityUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString
@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
public class DuoSecurityUserAccount implements Serializable {

    private static final long serialVersionUID = 403995883439339241L;

    private final String username;

    private final Map<String, String> metadata = new LinkedHashMap<>();

    private DuoSecurityUserAccountStatus status = DuoSecurityUserAccountStatus.AUTH;

    private String enrollPortalUrl;

    private String message;

    private List<DuoSecurityUserDevice> devices = new ArrayList<>();

    /**
     * Add devices.
     *
     * @param device the device
     * @return the duo security user account
     */
    public DuoSecurityUserAccount addDevice(final DuoSecurityUserDevice device) {
        this.devices.add(device);
        return this;
    }

    /**
     * Add attribute.
     *
     * @param key   the key
     * @param value the value
     * @return the duo security user account
     */
    public DuoSecurityUserAccount addAttribute(final String key, final String value) {
        this.metadata.put(key, value);
        return this;
    }

    public String getUserId() {
        return this.metadata.get("user_id");
    }

    public String getFirstName() {
        return this.metadata.get("firstname");
    }

    public String getLastName() {
        return this.metadata.get("lastname");
    }

    public String getEmail() {
        return this.metadata.get("email");
    }

    public String getRealName() {
        return this.metadata.get("realname");
    }

    public Instant getLastLogin() {
        return Instant.ofEpochMilli(Long.valueOf(metadata.getOrDefault("last_login", "0")));
    }

    public Instant getCreated() {
        return Instant.ofEpochMilli(Long.valueOf(metadata.getOrDefault("created", "0")));
    }

    public boolean isEnrolled() {
        return BooleanUtils.toBoolean(this.metadata.getOrDefault("is_enrolled", "false"));
    }
}
