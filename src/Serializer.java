import java.io.*;

class Serializer implements Serializable {
    static String[] getData(byte[] b, int reso) {
        StringBuilder command = new StringBuilder();
        StringBuilder name = new StringBuilder(), desc = new StringBuilder();
        int sep_count=0;
        char znak = ':';
        for(int i = 0; i < reso ; i++ ) {
            if(znak == ((char)b[i])) {
                sep_count++;
                i++;
            }
            if(sep_count == 0) {
                command.append((char) b[i]);
            }
            else if(sep_count == 1) {
                name.append((char) b[i]);
            }
            else if(sep_count == 2) {
                desc.append((char) b[i]);
            }
        }
        return new String[]{command.toString(), name.toString(), desc.toString()};
    }
}
