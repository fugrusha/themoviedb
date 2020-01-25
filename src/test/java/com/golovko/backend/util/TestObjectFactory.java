package com.golovko.backend.util;

import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Person;
import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TestObjectFactory {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PersonRepository personRepository;

    public Movie createMovie() {
        Movie movie = new Movie();
        movie.setMovieTitle("Title of the Movie");
        movie.setDescription("movie description");
        movie.setIsReleased(false);
        movie.setReleaseDate(LocalDate.parse("1990-05-14"));
        movie.setAverageRating(5.0);
        movie = movieRepository.save(movie);
        return movie;
    }

    public Person createPerson() {
        Person person = new Person();
        person.setFirstName("Anna");
        person.setLastName("Popova");
        person.setGender(Gender.FEMALE);
        person = personRepository.save(person);
        return person;
    }
}
