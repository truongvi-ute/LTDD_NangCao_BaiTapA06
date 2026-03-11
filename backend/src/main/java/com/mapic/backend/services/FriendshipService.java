package com.mapic.backend.services;

import com.mapic.backend.dtos.FriendshipDto;
import com.mapic.backend.entities.Friendship;
import com.mapic.backend.entities.FriendshipStatus;
import com.mapic.backend.entities.User;
import com.mapic.backend.repositories.FriendshipRepository;
import com.mapic.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipService {
    
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void sendFriendRequest(Long requesterId, String addresseeUsername) {
        // Find addressee by username
        User addressee = userRepository.findByUsername(addresseeUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        // Check if trying to add self
        if (requesterId.equals(addressee.getId())) {
            throw new RuntimeException("Không thể kết bạn với chính mình");
        }
        
        // Check if friendship already exists
        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(requesterId, addressee.getId());
        if (existing.isPresent()) {
            Friendship friendship = existing.get();
            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new RuntimeException("Đã là bạn bè");
            } else if (friendship.getStatus() == FriendshipStatus.PENDING) {
                throw new RuntimeException("Lời mời kết bạn đã được gửi");
            } else if (friendship.getStatus() == FriendshipStatus.BLOCKED) {
                throw new RuntimeException("Không thể gửi lời mời kết bạn");
            }
        }
        
        // Create new friendship request
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatus(FriendshipStatus.PENDING);
        
        friendshipRepository.save(friendship);
        log.info("Friend request sent from {} to {}", requesterId, addressee.getId());
    }
    
    @Transactional
    public void acceptFriendRequest(Long userId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Lời mời không tồn tại"));
        
        // Check if user is the addressee
        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền chấp nhận lời mời này");
        }
        
        // Check if still pending
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("Lời mời không hợp lệ");
        }
        
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
        log.info("Friend request accepted: {}", friendshipId);
    }
    
    @Transactional
    public void rejectFriendRequest(Long userId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Lời mời không tồn tại"));
        
        // Check if user is the addressee
        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền từ chối lời mời này");
        }
        
        friendshipRepository.delete(friendship);
        log.info("Friend request rejected: {}", friendshipId);
    }
    
    @Transactional
    public void unfriend(Long userId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Quan hệ bạn bè không tồn tại"));
        
        // Check if user is part of this friendship
        if (!friendship.getRequester().getId().equals(userId) && 
            !friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền hủy kết bạn");
        }
        
        friendshipRepository.delete(friendship);
        log.info("Unfriended: {}", friendshipId);
    }
    
    public List<FriendshipDto> getFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED);
        List<FriendshipDto> result = new ArrayList<>();
        
        for (Friendship friendship : friendships) {
            User friend = friendship.getRequester().getId().equals(userId) 
                    ? friendship.getAddressee() 
                    : friendship.getRequester();
            
            FriendshipDto dto = new FriendshipDto();
            dto.setId(friendship.getId());
            dto.setUserId(friend.getId());
            dto.setUsername(friend.getUsername());
            dto.setName(friend.getName());
            dto.setAvatarUrl(UserService.buildAvatarUrl(friend.getProfile() != null ? friend.getProfile().getAvatarUrl() : null));
            dto.setStatus("ACCEPTED");
            dto.setCreatedAt(friendship.getCreatedAt().toString());
            
            result.add(dto);
        }
        
        return result;
    }
    
    public List<FriendshipDto> getPendingRequests(Long userId) {
        List<Friendship> received = friendshipRepository.findPendingRequestsReceived(userId);
        List<FriendshipDto> result = new ArrayList<>();
        
        for (Friendship friendship : received) {
            User requester = friendship.getRequester();
            
            FriendshipDto dto = new FriendshipDto();
            dto.setId(friendship.getId());
            dto.setUserId(requester.getId());
            dto.setUsername(requester.getUsername());
            dto.setName(requester.getName());
            dto.setAvatarUrl(UserService.buildAvatarUrl(requester.getProfile() != null ? requester.getProfile().getAvatarUrl() : null));
            dto.setStatus("PENDING");
            dto.setType("RECEIVED");
            dto.setCreatedAt(friendship.getCreatedAt().toString());
            
            result.add(dto);
        }
        
        return result;
    }
    
    public List<User> searchUserByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }
}
