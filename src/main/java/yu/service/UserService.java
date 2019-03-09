package yu.service;

import yu.netty.ChatMsg;
import yu.pojo.Users;
import yu.pojo.vo.FriendRequestVO;
import yu.pojo.vo.MyFriendsVO;

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


	/**
	 * 查询添加好友请求
	 * @param acceptUserId
	 * @return
	 */
	public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);


	/**
	 * 删除好友添加请求
	 * @param sendUserId
	 * @param acceptUserId
	 */
	public void deleteFriendRequest(String sendUserId, String acceptUserId);


	/**
	 * 通过好友添加请求
	 * @param sendUserId
	 * @param acceptUserId
	 */
	public void passFriendRequest(String sendUserId, String acceptUserId);


	/**
	 * 保存好友
	 * @param sendUserId
	 * @param acceptUserId
	 */
	public void saveFriends(String sendUserId, String acceptUserId) ;

	/**
	 * 查询好友列表
	 * @param userId
	 * @return
	 */
	public List<MyFriendsVO> queryMyFriends(String userId);

	/**
	 *  保存聊天信息到数据库
	 * @param chatMsg
	 * @return
	 */
	public String saveMsg(ChatMsg chatMsg) ;

	/**
	 * 批量签收消息
	 * @param msgIdList
	 */
	public void updateMsgSigned(List<String> msgIdList) ;


	/**
	 * 查询没有签收的消息列表
	 * @param acceptUserId
	 * @return
	 */
	public List<yu.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) ;



	}
