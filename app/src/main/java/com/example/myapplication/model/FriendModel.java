package com.example.myapplication.model;

public class FriendModel {
    String DateRequest;

    public FriendModel(String dateRequest, String dateAccept) {
        DateRequest = dateRequest;
        DateAccept = dateAccept;
    }

    public String getDateAccept() {
        return DateAccept;
    }

    public void setDateAccept(String dateAccept) {
        DateAccept = dateAccept;
    }

    String DateAccept;

}
