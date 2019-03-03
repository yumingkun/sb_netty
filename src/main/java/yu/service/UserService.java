package yu.service;

import yu.pojo.Users;

import java.util.List;



public interface UserService {

	/**
	 * @Description: 判断用户名是否存在
	 */
	public boolean queryUsernameIsExist(String username);
	
	/**
	 * @Description: 查询用户是否存在
	 */
	public Users queryUserForLogin(String username, String pwd);

	/**
	 * @Description: 用户注册
	 */
	public Users saveUser(Users user);

	/**
	 * @Description: 修改用户记录
	 */
	public Users updateUserInfo(Users user);

	public  Users queryUserById(String userId);

	/**
	 * @Description: 搜索朋友的前置条件
	 */
	public Integer preconditionSearchFriends(String myUserId, String friendUsername);

	/**
	 * @Description: 根据用户名查询用户对象
	 */
	public Users queryUserInfoByUsername(String username);

	/**
	 * @Description: 添加好友请求记录，保存到数据库
	 */
	public void sendFriendRequest(String myUserId, String friendUsername);


}
