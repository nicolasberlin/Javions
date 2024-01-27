package ch.epfl.javions.adsb;

/**
 * Transforms a raw ADB-S message to an identification, a position or a velocity message
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public final class MessageParser {

    private final static int MIN_TYPE_CODE_AIM = 1;
    private final static int MAX_TYPE_CODE_AIM = 4;
    private final static int TYPE_CODE_AVM = 19;
    private final static int LOWER_MIN_TYPE_CODE_APM = 9;
    private final static int LOWER_MAX_TYPE_CODE_APM = 18;
    private final static int HIGHER_MIN_TYPE_CODE_APM = 20;
    private final static int HIGHER_MAX_TYPE_CODE_APM = 22;


    private MessageParser() {
    }

    /**
     * Returns an identification, a position or a velocity messages
     * corresponding to the given raw message
     * otherwise it returns null if the type code of message does not match
     *
     * @param rawMessage given to verify which messages should return
     * @return an identification, a position or a velocity messages otherwise it returns null
     */
    public static Message parse(RawMessage rawMessage) {
        int typeCode = rawMessage.typeCode();
        if (MIN_TYPE_CODE_AIM <= typeCode && typeCode <= MAX_TYPE_CODE_AIM) {
            return AircraftIdentificationMessage.of(rawMessage);
        } else if ((LOWER_MIN_TYPE_CODE_APM <= typeCode && typeCode <= LOWER_MAX_TYPE_CODE_APM)
                || (HIGHER_MIN_TYPE_CODE_APM <= typeCode && typeCode <= HIGHER_MAX_TYPE_CODE_APM)) {
            return AirbornePositionMessage.of(rawMessage);
        } else if (typeCode == TYPE_CODE_AVM) {
            return AirborneVelocityMessage.of(rawMessage);
        }
        return null;

    }
}
