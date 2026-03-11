package com.mapic.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDto {
    private Long id;
    private String name;
    private String description;
    private Long itemCount;
    private LocalDateTime createdAt;
    private List<MomentDto> moments;
}
