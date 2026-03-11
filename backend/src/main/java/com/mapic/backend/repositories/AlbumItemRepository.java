package com.mapic.backend.repositories;

import com.mapic.backend.entities.Album;
import com.mapic.backend.entities.AlbumItem;
import com.mapic.backend.entities.Moment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumItemRepository extends JpaRepository<AlbumItem, Long> {

    List<AlbumItem> findByAlbumOrderBySavedAtDesc(Album album);

    Page<AlbumItem> findByAlbumOrderBySavedAtDesc(Album album, Pageable pageable);
    
    Optional<AlbumItem> findByAlbumAndMoment(Album album, Moment moment);

    boolean existsByAlbumAndMoment(Album album, Moment moment);

    long countByAlbum(Album album);
    
    void deleteByAlbumAndMoment(Album album, Moment moment);
}
