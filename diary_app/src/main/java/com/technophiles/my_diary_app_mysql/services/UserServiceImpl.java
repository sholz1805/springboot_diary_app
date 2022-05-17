package com.technophiles.my_diary_app_mysql.services;

import com.technophiles.my_diary_app_mysql.dtos.UserDto;
import com.technophiles.my_diary_app_mysql.exceptions.DiaryApplicationException;
import com.technophiles.my_diary_app_mysql.exceptions.UserNotFoundException;
import com.technophiles.my_diary_app_mysql.models.Diary;
import com.technophiles.my_diary_app_mysql.models.User;
import com.technophiles.my_diary_app_mysql.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Optional;

@Service
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class UserServiceImpl implements UserService, UserDetailsService {

    private UserRepository userRepository;
    private  ModelMapper modelMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
        this.modelMapper = new ModelMapper();
    }


    @Override
    public UserDto createAccount(String email, String password) throws DiaryApplicationException {
        Optional<User> userOptional = userRepository.findUserByEmail(email);
        if(userOptional.isEmpty()){
            User user = new User(email, password);
            User savedUser = userRepository.save(user);
            return modelMapper.map(savedUser, UserDto.class);
        }
        throw new DiaryApplicationException("User already exists");
    }

    @Override
    public Diary addDiary(Long id, Diary diary) throws DiaryApplicationException {
        User user  = userRepository.findById(id).orElseThrow(() -> new DiaryApplicationException("User with this id does noe exist"));
        user.addDiary(diary);
        userRepository.save(user);
        return diary;
    }

    @Override
    public User findById(Long id) throws DiaryApplicationException {
        return userRepository.findById(id).orElseThrow(() -> new DiaryApplicationException("user does not exist"));
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User with this email not found"));
    }

    @Override
    public boolean deleteUser(User user) {
        userRepository.delete(user);
        return true;
    }

    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException("user not found"));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}
