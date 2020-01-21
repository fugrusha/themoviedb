package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Report;
import com.golovko.backend.dto.*;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    /*
        ApplicationUser translations
    */
    public UserReadDTO toRead(ApplicationUser applicationUser) {
        UserReadDTO dto = new UserReadDTO();
        dto.setId(applicationUser.getId());
        dto.setUsername(applicationUser.getUsername());
        dto.setPassword(applicationUser.getPassword());
        dto.setEmail(applicationUser.getEmail());
        return dto;
    }

    public ApplicationUser toEntity(UserCreateDTO createDTO) {
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setUsername(createDTO.getUsername());
        applicationUser.setEmail(createDTO.getEmail());
        applicationUser.setPassword(createDTO.getPassword());
        return applicationUser;
    }

    public void patchEntity(UserPatchDTO patchDTO, ApplicationUser applicationUser) {
        if (patchDTO.getUsername() != null) {
            applicationUser.setUsername(patchDTO.getUsername()); // username cannot be editable
        }
        if (patchDTO.getEmail() != null){
            applicationUser.setEmail(patchDTO.getEmail());
        }
        if (patchDTO.getPassword() != null){
            applicationUser.setPassword(patchDTO.getPassword());
        }
    }


    /*
        Movie translations
    */
    public MovieReadDTO toRead(Movie movie) {
        MovieReadDTO dto = new MovieReadDTO();
        dto.setId(movie.getId());
        dto.setMovieTitle(movie.getMovieTitle());
        dto.setDescription(movie.getDescription());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setReleased(movie.getIsReleased());
        dto.setAverageRating(movie.getAverageRating());
        return dto;
    }

    public Movie toEntity(MovieCreateDTO createDTO) {
        Movie movie = new Movie();
        movie.setMovieTitle(createDTO.getMovieTitle());
        movie.setDescription(createDTO.getDescription());
        movie.setReleaseDate(createDTO.getReleaseDate());
        movie.setIsReleased(createDTO.isReleased());
        movie.setAverageRating(createDTO.getAverageRating());
        return movie;
    }

    public void patchEntity(MoviePatchDTO patchDTO, Movie movie) {
        if (patchDTO.getMovieTitle() != null){
            movie.setMovieTitle(patchDTO.getMovieTitle());
        }
        if (patchDTO.getDescription() != null) {
            movie.setDescription(patchDTO.getDescription());
        }
        if (patchDTO.getAverageRating() != null){
            movie.setAverageRating(patchDTO.getAverageRating());
        }
        if (patchDTO.getReleaseDate() != null){
            movie.setReleaseDate(patchDTO.getReleaseDate());
        }
        if (patchDTO.getIsReleased() != null){
            movie.setIsReleased(patchDTO.getIsReleased());
        }
    }

    /*
        Report translations
    */
    public ReportReadDTO toRead(Report report) {
        ReportReadDTO dto = new ReportReadDTO();
        dto.setId(report.getId());
        dto.setReportTitle(report.getReportTitle());
        dto.setReportText(report.getReportText());
        dto.setReportType(report.getReportType());
        return dto;
    }

    public Report toEntity(ReportCreateDTO createDTO) {
        Report report = new Report();
        report.setReportTitle(createDTO.getReportTitle());
        report.setReportText(createDTO.getReportText());
        report.setReportType(createDTO.getReportType());
        return report;
    }

    public void patchEntity(ReportPatchDTO patchDTO, Report report) {
        if (patchDTO.getReportTitle() != null) {
            report.setReportTitle(patchDTO.getReportTitle());
        }
        if (patchDTO.getReportText() != null) {
            report.setReportText(patchDTO.getReportText());
        }
        if (patchDTO.getReportType() != null) {
            report.setReportType(patchDTO.getReportType());
        }
    }
}
