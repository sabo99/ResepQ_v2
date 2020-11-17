package com.sabo.resepq_v2.Helper;

import com.sabo.resepq_v2.RoomDB.RecipeModel;

public class Common {
    public static final int REQUEST_PERMISSION_UPLOAD = 101;
    public static RecipeModel selectedRecipe;

    public static String formatDateInDetail(String datetime) {
        String result = "", month, day;
        String m = datetime.substring(5, 7);
        String d = datetime.substring(8, 10);

        month = getMonth(m);
        day = d;

        result = month + " " + day;
        return result;
    }

    private static String getMonth(String m) {
        switch (Integer.parseInt(m)) {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
            default:
                return "";
        }
    }
}
