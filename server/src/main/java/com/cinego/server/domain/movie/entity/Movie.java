package com.cinego.server.domain.movie.entity;

import com.cinego.server.common.entity.BaseEntity;
import com.cinego.server.domain.review.entity.Review;
import com.cinego.server.domain.showtime.entity.Showtime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "movies",
        indexes = {
                @Index(name = "idx_movie_status", columnList = "status"),
                @Index(name = "idx_movie_release_date", columnList = "release_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movie extends BaseEntity {

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieGenre> movieGenres = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Showtime> showtimes = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Thời lượng phim (phút).
     */
    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", length = 16)
    private AgeRating rating;

    @Column(name = "language", length = 16)
    private String language = "vi";

    @Column(name = "director")
    private String director;

    /**
     * Danh sách diễn viên, lưu dạng text (ví dụ: "Actor 1, Actor 2").
     * Dùng tên cột 'cast_names' để tránh trùng keyword SQL CAST.
     */
    @Column(name = "cast_names", columnDefinition = "text")
    private String cast;

    @Column(name = "trailer_url", columnDefinition = "text")
    private String trailerUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32)
    private MovieStatus status = MovieStatus.COMING_SOON;

    @Column(name = "imdb_rating", precision = 3, scale = 1)
    private BigDecimal imdbRating;

    @Column(name = "country")
    private String country;

    public enum MovieStatus {
        COMING_SOON,
        NOW_SHOWING,
        ENDED
    }

    public enum AgeRating {
        G,
        PG,
        PG_13,
        R
    }
}

