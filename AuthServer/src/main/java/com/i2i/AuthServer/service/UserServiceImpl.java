package com.i2i.AuthServer.service;

import com.i2i.AuthServer.dto.UserDto;
import com.i2i.AuthServer.dto.UserResponseDto;
import com.i2i.AuthServer.exceptionHandling.DataBaseException;
import com.i2i.AuthServer.exceptionHandling.UserAlreadyPresent;
import com.i2i.AuthServer.exceptionHandling.UserNotFoundException;
import com.i2i.AuthServer.model.Role;
import com.i2i.AuthServer.model.User;
import com.i2i.AuthServer.repository.RoleRepository;
import com.i2i.AuthServer.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;


    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    public UserResponseDto createUser(UserDto userDto)
    {
        Optional<User> existingUser =userRepository.findByUsername(userDto.getUsername());
        if(existingUser.isEmpty())
         try {
            User user = modelMapper.map(userDto,User.class);
             Set<Role> incomingRoles = new HashSet<>(user.getRoles());
             Set<Role> resolvedRoles = incomingRoles.stream()
                     .map(role -> roleRepository.findByName(role.getName())
                             .orElseThrow(() -> new RuntimeException("Role not found: " + role.getName())))
                     .collect(Collectors.toSet());

             user.setRoles(resolvedRoles);
             user.setPassword(passwordEncoder.encode(user.getPassword()));
             return modelMapper.map(userRepository.save(user),UserResponseDto.class);
        }catch (Exception ex)
        {
            log.error("Exception occurred: "+ex);
            throw new DataBaseException(ex.getMessage());
        }
        else{
            throw new UserAlreadyPresent("User already exists.Please try with other UserName");
        }

    }

    public UserResponseDto updateUser(String userName, UserDto userDTO,String loggedUser) {
        Optional<User> existingUser = userRepository.findByUsername(userName);
        if(existingUser.isPresent())
        {
            User user = modelMapper.map(userDTO,User.class);
            user.setId(existingUser.get().getId());

            user.setCreatedDate(existingUser.get().getCreatedDate());
            user.setCreatedBy(existingUser.get().getCreatedBy());

            user.setLastModifiedBy(loggedUser);
            user.setLastModifiedDate(LocalDateTime.now());

            Set<Role> incomingRoles = new HashSet<>(user.getRoles());
            Set<Role> resolvedRoles = incomingRoles.stream()
                    .map(role -> roleRepository.findByName(role.getName())
                            .orElseThrow(() -> new RuntimeException("Role not found: " + role.getName())))
                    .collect(Collectors.toSet());
            user.setRoles(resolvedRoles);
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

            return modelMapper.map(userRepository.save(user),UserResponseDto.class);
        }else {
            throw new UserNotFoundException("User not Found");
        }

    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    public UserResponseDto disableUser(Long id,String loggedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(false);

        user.setLastModifiedBy(loggedUser);
        user.setLastModifiedDate(LocalDateTime.now());
        return modelMapper.map(userRepository.save(user),UserResponseDto.class);
    }


    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream().map(user -> modelMapper.map(user,UserResponseDto.class)).collect(Collectors.toList());
    }
}
