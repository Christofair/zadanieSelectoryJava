import com.sun.xml.internal.fastinfoset.util.StringArray;

import java.io.*;
import java.util.Vector;

public class Temat {
    String nazwa;
    String opis;

    Temat() {
        nazwa = "";
        opis = "";
    }
    Temat(String nazwa, String opis) {
        this.nazwa = nazwa;
        this.opis = opis;
    }

    @Override
    public String toString() {
        return nazwa;
    }

    public String getNazwa() {
        return toString();
    }

    public String getOpis() {
        return opis;
    }
}
