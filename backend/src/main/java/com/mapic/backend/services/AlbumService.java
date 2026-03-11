package com.mapic.backend.services;

import com.mapic.backend.dtos.AlbumDto;
import com.mapic.backend.dtos.CreateAlbumRequest;
import com.mapic.backend.dtos.MomentDto;
import com.mapic.backend.entities.Album;
import com.mapic.backend.entities.AlbumItem;
import com.mapic.backend.entities.Moment;
import com.mapic.backend.entities.User;
import com.mapic.backend.repositories.AlbumItemRepository;
import com.mapic.backend.repositories.AlbumRepository;
import com.mapic.backend.repositories.MomentRepository;
import com.mapic.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumItemRepository albumItemRepository;
    private final UserRepository userRepository;
    private final MomentRepository momentRepository;
    private final MomentService momentService;

    public List<AlbumDto> getUserAlbums(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Album> albums = albumRepository.findByUserOrderByCreatedAtDesc(user);

        return albums.stream()
                .map(this::convertToDtoWithoutMoments)
                .collect(Collectors.toList());
    }

    public AlbumDto getAlbumDetails(Long albumId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = albumRepository.findByIdAndUser(albumId, user)
                .orElseThrow(() -> new RuntimeException("Album not found or you don't have permission"));

        List<AlbumItem> items = albumItemRepository.findByAlbumOrderBySavedAtDesc(album);
        
        List<MomentDto> momentDtos = items.stream()
                .map(item -> momentService.convertToDto(item.getMoment(), userId))
                .collect(Collectors.toList());

        AlbumDto dto = convertToDtoWithoutMoments(album);
        dto.setMoments(momentDtos);
        
        return dto;
    }

    @Transactional
    public AlbumDto createAlbum(Long userId, CreateAlbumRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (albumRepository.existsByNameAndUser(request.getName(), user)) {
            throw new RuntimeException("Album name already exists");
        }

        Album album = new Album();
        album.setName(request.getName());
        album.setDescription(request.getDescription());
        album.setUser(user);

        Album savedAlbum = albumRepository.save(album);
        return convertToDtoWithoutMoments(savedAlbum);
    }

    @Transactional
    public AlbumDto updateAlbum(Long albumId, Long userId, CreateAlbumRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = albumRepository.findByIdAndUser(albumId, user)
                .orElseThrow(() -> new RuntimeException("Album not found or permission denied"));

        if (!album.getName().equals(request.getName()) && 
            albumRepository.existsByNameAndUser(request.getName(), user)) {
            throw new RuntimeException("Album name already exists");
        }

        album.setName(request.getName());
        album.setDescription(request.getDescription());

        Album updatedAlbum = albumRepository.save(album);
        return convertToDtoWithoutMoments(updatedAlbum);
    }

    @Transactional
    public void deleteAlbum(Long albumId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = albumRepository.findByIdAndUser(albumId, user)
                .orElseThrow(() -> new RuntimeException("Album not found or permission denied"));

        albumRepository.delete(album);
    }

    @Transactional
    public AlbumDto addMomentToAlbum(Long albumId, Long momentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = albumRepository.findByIdAndUser(albumId, user)
                .orElseThrow(() -> new RuntimeException("Album not found or permission denied"));

        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));

        if (albumItemRepository.existsByAlbumAndMoment(album, moment)) {
            throw new RuntimeException("Moment is already in this album");
        }

        AlbumItem albumItem = new AlbumItem();
        albumItem.setAlbum(album);
        albumItem.setMoment(moment);
        albumItemRepository.save(albumItem);

        // Update album item count virtually or relies on count query.
        return convertToDtoWithoutMoments(album);
    }

    @Transactional
    public AlbumDto removeMomentFromAlbum(Long albumId, Long momentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Album album = albumRepository.findByIdAndUser(albumId, user)
                .orElseThrow(() -> new RuntimeException("Album not found or permission denied"));

        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));

        AlbumItem albumItem = albumItemRepository.findByAlbumAndMoment(album, moment)
                .orElseThrow(() -> new RuntimeException("Moment is not in this album"));

        albumItemRepository.delete(albumItem);

        return convertToDtoWithoutMoments(album);
    }

    private AlbumDto convertToDtoWithoutMoments(Album album) {
        AlbumDto dto = new AlbumDto();
        dto.setId(album.getId());
        dto.setName(album.getName());
        dto.setDescription(album.getDescription());
        dto.setCreatedAt(album.getCreatedAt());
        // Fast counting without fetching all items
        long count = albumItemRepository.countByAlbum(album);
        dto.setItemCount(count);
        return dto;
    }
}
