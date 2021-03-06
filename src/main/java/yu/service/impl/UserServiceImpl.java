package yu.service.impl;


import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;
import yu.enums.MsgActionEnum;
import yu.enums.MsgSignFlagEnum;
import yu.enums.SearchFriendsStatusEnum;
import yu.mapper.*;
import yu.netty.DataContent;
import yu.netty.UserChannelRel;
import yu.netty.ChatMsg;
import yu.pojo.FriendsRequest;
import yu.pojo.MyFriends;
import yu.pojo.Users;
import yu.pojo.vo.FriendRequestVO;
import yu.pojo.vo.MyFriendsVO;
import yu.service.UserService;
import yu.util.FastDFSClient;
import yu.util.FileUtils;
import yu.util.JsonUtils;
import yu.util.QRCodeUtils;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UsersMapper userMapper;


	@Autowired
	private Sid sid;

	@Autowired
	private QRCodeUtils qrCodeUtils;
	@Autowired
	private FastDFSClient fastDFSClient;



	@Autowired
	private UsersMapperCustom usersMapperCustom;

	@Autowired
	private MyFriendsMapper myFriendsMapper;

	@Autowired
	private FriendsRequestMapper friendsRequestMapper;

	@Autowired
	private ChatMsgMapper chatMsgMapper;



	@Override
	public boolean queryUsernameIsExist(String username) {
		
		Users user = new Users();
		user.setUsername(username);
		
		Users result = userMapper.selectOne(user);
		
		return result != null ? true : false;
	}

	@Override
	public Users queryUserForLogin(String username, String pwd) {

		Example userExample=new Example(Users.class);

		Example.Criteria criteria = userExample.createCriteria();

		criteria.andEqualTo("username", username);
		criteria.andEqualTo("password", pwd);

		Users result = userMapper.selectOneByExample(userExample);

		return result;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public Users saveUser(Users user) {

		String userId = sid.nextShort();


		// 为每个用户生成一个唯一的二维码
		String qrCodePath = "/yu_photo/" + userId + "qrcode.png";
//		 muxin_qrcode:[username]
		qrCodeUtils.createQRCode(qrCodePath, "muxin_qrcode:" + user.getUsername());
		MultipartFile qrCodeFile = FileUtils.fileToMultipart(qrCodePath);

		String qrCodeUrl = "";
		try {
			qrCodeUrl = fastDFSClient.uploadQRCode(qrCodeFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		user.setQrcode(qrCodeUrl);
		user.setId(userId);
		userMapper.insert(user);

		return user;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public Users updateUserInfo(Users user) {
		userMapper.updateByPrimaryKeySelective(user);
		System.out.printf("server====================="+user.toString());
		return queryUserById(user.getId());
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	public Users queryUserById(String userId) {
		return userMapper.selectByPrimaryKey(userId);
	}

	/**
	 * 搜索朋友的前置条件
	 * @param myUserId
	 * @param friendUsername
	 * @return
	 */
	@Override
	@Transactional(propagation = Propagation.SUPPORTS)
	public Integer preconditionSearchFriends(String myUserId, String friendUsername) {

		 //1:如果用户不存在，返回无此用户
		Users user=queryUserInfoByUsername(friendUsername);
		if (user==null){
			return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
		}
		//2：搜索账号如果是你自己，返回不能添加自己
		if (user.getId().equals(myUserId)){
			return SearchFriendsStatusEnum.NOT_YOURSELF.status;
		}
		//3：搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
		Example mfe=new Example(MyFriends.class);
		Example.Criteria ufc=mfe.createCriteria();
		ufc.andEqualTo("myUserId",myUserId);
		ufc.andEqualTo("myFriendUserId",user.getId());
		MyFriends myFriends= myFriendsMapper.selectOneByExample(mfe);
		if (myFriends!=null){
			return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
		}

		//返回可以添加
		return SearchFriendsStatusEnum.SUCCESS.status;
	}

	/**
	 * 根据用户名查询用户是否存在
	 * @param username
	 * @return
	 */

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public Users queryUserInfoByUsername(String username) {
		Example ue=new Example(Users.class);
		Example.Criteria uc=ue.createCriteria();
		uc.andEqualTo("username",username);
		return  userMapper.selectOneByExample(ue);
	}


	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void sendFriendRequest(String myUserId, String friendUsername) {

		// 根据用户名把朋友信息查询出来
		Users friend = queryUserInfoByUsername(friendUsername);

		// 1. 查询发送好友请求记录表
		Example fre = new Example(FriendsRequest.class);
		Example.Criteria frc = fre.createCriteria();
		frc.andEqualTo("sendUserId", myUserId);
		frc.andEqualTo("acceptUserId", friend.getId());
		FriendsRequest friendRequest = friendsRequestMapper.selectOneByExample(fre);
		if (friendRequest == null) {
			// 2. 如果不是你的好友，并且好友记录没有添加，则新增好友请求记录
			String requestId = sid.nextShort();

			FriendsRequest request = new FriendsRequest();
			request.setId(requestId);
			request.setSendUserId(myUserId);
			request.setAcceptUserId(friend.getId());
			request.setRequestDateTime(new Date());
			friendsRequestMapper.insert(request);
		}
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
		return usersMapperCustom.queryFriendRequestList(acceptUserId);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void deleteFriendRequest(String sendUserId, String acceptUserId) {
		Example fre = new Example(FriendsRequest.class);
		Example.Criteria frc = fre.createCriteria();
		frc.andEqualTo("sendUserId", sendUserId);
		frc.andEqualTo("acceptUserId", acceptUserId);
		friendsRequestMapper.deleteByExample(fre);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void passFriendRequest(String sendUserId, String acceptUserId) {
		saveFriends(sendUserId, acceptUserId);
		saveFriends(acceptUserId, sendUserId);
		deleteFriendRequest(sendUserId, acceptUserId);

		Channel sendChannel = UserChannelRel.get(sendUserId);
		if (sendChannel != null) {
			// 使用websocket主动推送消息到请求发起者，更新他的通讯录列表为最新
			DataContent dataContent = new DataContent();
			dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);

			sendChannel.writeAndFlush(
					new TextWebSocketFrame(
							JsonUtils.objectToJson(dataContent)));
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public  void saveFriends(String sendUserId, String acceptUserId) {
		MyFriends myFriends = new MyFriends();
		String recordId = sid.nextShort();
		myFriends.setId(recordId);
		myFriends.setMyFriendUserId(acceptUserId);
		myFriends.setMyUserId(sendUserId);
		myFriendsMapper.insert(myFriends);
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public List<MyFriendsVO> queryMyFriends(String userId) {
		List<MyFriendsVO> myFirends = usersMapperCustom.queryMyFriends(userId);
		return myFirends;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public String saveMsg(ChatMsg chatMsg) {

		yu.pojo.ChatMsg msgDB = new  yu.pojo.ChatMsg();
		String msgId = sid.nextShort();
		msgDB.setId(msgId);
		msgDB.setAcceptUserId(chatMsg.getReceiverId());
		msgDB.setSendUserId(chatMsg.getSenderId());
		msgDB.setCreateTime(new Date());
		msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
		msgDB.setMsg(chatMsg.getMsg());

		chatMsgMapper.insert(msgDB);

		return msgId;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void updateMsgSigned(List<String> msgIdList) {
		usersMapperCustom.batchUpdateMsgSigned(msgIdList);
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public List<yu.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {

		Example chatExample = new Example(yu.pojo.ChatMsg.class);
		Example.Criteria chatCriteria = chatExample.createCriteria();
		chatCriteria.andEqualTo("signFlag", 0);
		chatCriteria.andEqualTo("acceptUserId", acceptUserId);

		List<yu.pojo.ChatMsg> chatMsgList = chatMsgMapper.selectByExample(chatExample);

		return chatMsgList;
	}
}

