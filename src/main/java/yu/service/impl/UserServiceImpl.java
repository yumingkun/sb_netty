package yu.service.impl;


import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;
import yu.enums.SearchFriendsStatusEnum;
import yu.mapper.MyFriendsMapper;
import yu.mapper.UsersMapper;
import yu.pojo.MyFriends;
import yu.pojo.Users;
import yu.service.UserService;
import yu.util.FastDFSClient;
import yu.util.FileUtils;
import yu.util.QRCodeUtils;

import java.io.IOException;

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
	private MyFriendsMapper myFriendsMapper;


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
		String qrCodePath = "/Users/mingkunyu/Downloads/" + userId + "qrcode.png";
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
	@Override
	public Users queryUserInfoByUsername(String username) {
		Example ue=new Example(Users.class);
		Example.Criteria uc=ue.createCriteria();
		uc.andEqualTo("username",username);
		return  userMapper.selectOneByExample(ue);
	}


	@Override
	public void sendFriendRequest(String myUserId, String friendUsername) {

	}


}
