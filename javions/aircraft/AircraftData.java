package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * Collects information about aircraft
 *
 * @param registration           registration's number
 * @param typeDesignator         type designator
 * @param model                  of aircraft
 * @param description            of aircraft
 * @param wakeTurbulenceCategory category's turbulence
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */
public record AircraftData(AircraftRegistration registration,
                           AircraftTypeDesignator typeDesignator,
                           String model,
                           AircraftDescription description,
                           WakeTurbulenceCategory wakeTurbulenceCategory) {

    /**
     * Checks if one element of the information is null or not
     *
     * @throws NullPointerException if one of the elements of the information is null.
     */
    public AircraftData {
        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);
    }

}
