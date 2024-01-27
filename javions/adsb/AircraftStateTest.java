package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AircraftStateTest implements AircraftStateSetter {
    @Override
    public void setLastMessageTimeStampNs(long timeStampsNs) {
        //System.out.println("horodatage : " + timeStampsNs);
    }

    @Override
    public void setCategory(int category) {
        // System.out.println("categorie : " + category);
    }

    @Override
    public void setCallSign(CallSign callSign) {
        System.out.println("indicatif : " + callSign);
    }

    @Override
    public void setPosition(GeoPos position) {
        System.out.println("position : " + position);
    }

    @Override
    public void setAltitude(double altitude) {
        // System.out.println("altitude : " + altitude);
    }

    @Override
    public void setVelocity(double velocity) {
        // System.out.println("vitesse : " + velocity);
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        // System.out.println("cap : " + trackOrHeading);
    }

    public static void main(String args[]) throws IOException {
        String f = "C:\\Users\\Lenovo\\Desktop\\Projets\\javions_skeleton\\Javions\\resources\\samples_20230304_1442.bin";
        IcaoAddress expectedAddress = new IcaoAddress("4241A9");
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            AircraftStateAccumulator<AircraftStateTest> a =
                    new AircraftStateAccumulator<>(new AircraftStateTest());
            int i = 1;
            while ((m = d.nextMessage()) != null) {
                if (!m.icaoAddress().equals(expectedAddress)) continue;
                Message pm = MessageParser.parse(m);
                if (pm != null) {
                    a.update(pm);
                    //System.out.println(i+ ")");
                    //System.out.println(pm);
                    ++i;
                }
            }
        }
    }
}
