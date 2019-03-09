package yu.mapper;

import yu.pojo.Users;
import yu.pojo.vo.FriendRequestVO;
import yu.pojo.vo.MyFriendsVO;
import yu.util.MyMapper;

import java.util.List;



public interface UsersMapperCustom extends MyMapper<Users> {
	
	public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);
	
	public List<MyFriendsVO> queryMyFriends(String userId);
	
	public void batchUpdateMsgSigned(List<String> msgIdList);
	
}