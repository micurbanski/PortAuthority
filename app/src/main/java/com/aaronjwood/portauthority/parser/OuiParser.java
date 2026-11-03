package com.aaronjwood.portauthority.parser;

import com.aaronjwood.portauthority.db.Database;

public class OuiParser implements Parser {

    /**
     * Parses the line of OUI data based on the IEEE's format.
     *
     * @param line
     * @return
     */
    @Override
    public String[] parseLine(String line) {
        if (line.isEmpty() || line.startsWith("#")) {
            return null;
        }

        String[] data = line.split("\\t");
        String mac = data[0].trim().toLowerCase();
        int slash = mac.indexOf('/');
        if (slash != -1) {
            // MA-M (/28) and MA-S (/36) blocks, e.g. "00:1b:c5:00:00/36" -> "00:1b:c5:00:0".
            int digits = Integer.parseInt(mac.substring(slash + 1)) / 4;
            mac = mac.substring(0, digits + (digits - 1) / 2);
        }

        String vendor;
        if (data.length == 3) {
            vendor = data[2];
        } else {
            vendor = data[1].trim();
        }

        return new String[]{mac, vendor};
    }

    /**
     * Exports the parsed line of OUI data to the database.
     *
     * @param db
     * @param line
     * @return
     */
    @Override
    public long exportLine(Database db, String[] line) {
        return db.insertOui(line[0], line[1]);
    }

}
