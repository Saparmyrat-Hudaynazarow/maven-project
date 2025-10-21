package com.song.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "song_metadata")
public class SongMetadata {
    @Id
    private Integer id;

    private String name;

    private String artist;

    private String album;

    private String duration;

    private String year;
}