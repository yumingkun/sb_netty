package yu.pojo;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name = "my_friends")
public class MyFriends {
    private String id;

    @Column(name = "my_user_id")
    private String myUserId;

    @Column(name = "my_friend_user_id")
    private Integer myFriendUserId;

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return my_user_id
     */
    public String getMyUserId() {
        return myUserId;
    }

    /**
     * @param myUserId
     */
    public void setMyUserId(String myUserId) {
        this.myUserId = myUserId;
    }

    /**
     * @return my_friend_user_id
     */
    public Integer getMyFriendUserId() {
        return myFriendUserId;
    }

    /**
     * @param myFriendUserId
     */
    public void setMyFriendUserId(Integer myFriendUserId) {
        this.myFriendUserId = myFriendUserId;
    }
}