package com.beautyteam.everpay.Database;

/**
 * Created by Admin on 04.04.2015.
 */
public class GroupDetails {
    static final String GROUP_DETAILS_TABLE = "group_details";

    static public final String ITEM_ID = "id";
    static private final String GROUP_ID = "group_id";
    static private final String USER_ID = "user_id";

    static public final String CREATE_TABLE = "create table " + GROUP_DETAILS_TABLE + "("
            + ITEM_ID + " integer primary key autoincrement, "
            + GROUP_ID + " integer,"
            + USER_ID + " integer,"
            + "FOREIGN KEY(" + GROUP_ID + ") REFERENCE" + Groups.GROUPS_TABLE+ "(" + Groups.GROUP_ID + ")"
            + "FOREIGN KEY(" + USER_ID + ") REFERENCE" + Users.USERS_TABLE + "(" + Users.USER_ID_VK + ")"
            + ")";

}