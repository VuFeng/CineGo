package com.cinego.server.domain.movie.entity;

import com.cinego.server.common.entity.BaseEntity;
import com.cinego.server.domain.genre.entity.Genre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "movie_genres",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_movie_genre",
                        columnNames = {"movie_id", "genre_id"}
                )
        },
        indexes = {
                @Index(name = "idx_movie_genre_movie", columnList = "movie_id"),
                @Index(name = "idx_movie_genre_genre", columnList = "genre_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenre extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;
}
