package com.project.moviebookingapp.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

@IgnoreExtraProperties
public class Account {
    private String accountId;
    private String userName;
    private String email;
    private String gender;
    private String phoneNo;
    private String birthDate;//store it as string for cross language implementation (python)
    private @ServerTimestamp Timestamp lastLoginTime;
    private String role; //user and admin
    private String profileImgURL;//get from firestore
    private List<String> recommendedList;
    public boolean allowNotShowing;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public Account(){ }

    //for item retrieval at classes
    //only used at edit profile activity
    public Account(String userName, String gender,String phoneNo,
                   String birthDate){
        this.userName = userName;
        this.gender = gender;
        this.phoneNo = phoneNo;
        this.birthDate = birthDate;
    }

    //to store in firebase
    public Account(FirebaseUser account,String userName, String email,String gender,String phoneNo,
                   String birthDate,Timestamp lastLoginTime, String role,String profileImgURL){
        this.accountId = account.getUid();
        this.userName = userName;
        this.email = email;
        this.gender = gender;
        this.phoneNo = phoneNo;
        this.birthDate = birthDate;
        this.lastLoginTime = lastLoginTime;
        this.role = role;
        this.profileImgURL = profileImgURL;
        //set recommendations field as empty for newbies
        recommendedList = new ArrayList<>();
        allowNotShowing = true;
    }

    public String getAccountId(){return accountId;}
    public Timestamp getLastLoginTime(){return lastLoginTime;}
    public String getUserName(){return userName;}
    public String getEmail(){return email;}
    public String getGender(){return gender;}
    public String getPhoneNo(){return phoneNo;}
    public String getBirthDate(){return birthDate;}
    public String getProfileImgURL(){return profileImgURL;}
    public String getRole(){return role;}
    public List<String> getRecommendedList() { return recommendedList; }
    public boolean getAllowNotShowing() { return allowNotShowing; }

    public void setAccountId(String accountId){ this.accountId = accountId; }
    public void setLastLoginTime(Timestamp lastLoginTime){this.lastLoginTime = lastLoginTime;}
    public void setUserName(String userName){
        this.userName = userName;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public void setGender(String gender){
        this.gender = gender;
    }
    public void setPhoneNo(String phoneNo){
        this.phoneNo = phoneNo;
    }
    public void setBirthDate(String birthDate){
        this.birthDate = birthDate;
    }
    public void setRole(String role){
        this.role = role;
    }
    public void setProfileImgURL(String profileImgURL){
        this.profileImgURL = profileImgURL;
    }
    public void setRecommendedList(List<String> recommendedList) {
        this.recommendedList = recommendedList;
    }
    public void setAllowNotShowing(boolean allowNotShowing) {
        this.allowNotShowing = allowNotShowing;
    }
}
