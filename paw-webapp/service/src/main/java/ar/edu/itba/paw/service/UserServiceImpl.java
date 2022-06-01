package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.*;
import ar.edu.itba.paw.model.exceptions.DuplicateUserException;
import ar.edu.itba.paw.model.exceptions.PageNotFoundException;
import ar.edu.itba.paw.model.exceptions.UserNotFoundException;
import ar.edu.itba.paw.persistence.UserDao;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private VerificationTokenService verificationTokenService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private GenreService genreService;
    @Autowired
    private MailingService mailingService;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final int MAX_USER_GENRES = 15;
    private static final int MAX_USER_ROLES = 15;

    @Override
    public Optional<User> getUserById(long id) {
        return userDao.getUserById(id);
    }

    @Transactional
    @Override
    public User create(User.UserBuilder userBuilder) {
        userBuilder.password(passwordEncoder.encode(userBuilder.getPassword()));
        if(userDao.findByEmail(userBuilder.getEmail()).isPresent()) {
            throw new DuplicateUserException();
        }
        User user = userDao.create(userBuilder);
        final VerificationToken token = verificationTokenService.generate(user, TokenType.VERIFY);
        Locale locale = LocaleContextHolder.getLocale();
        LocaleContextHolder.setLocale(locale, true);
        mailingService.sendVerificationEmail(user, token, locale);
        return user;
    }

    @Transactional
    @Override
    public void resendUserVerification(String email) {
        User user = findByEmail(email).orElseThrow(UserNotFoundException::new);

        verificationTokenService.deleteTokenByUserId(user.getId(), TokenType.VERIFY);

        VerificationToken token = verificationTokenService.generate(user, TokenType.VERIFY);

        Locale locale = LocaleContextHolder.getLocale();
        LocaleContextHolder.setLocale(locale, true);
        mailingService.sendVerificationEmail(user, token, locale);
    }

    @Transactional
    @Override
    public void editUser(long userId, String name, String surname, String description, List<String> genresNames, List<String> rolesNames, byte[] image) {
        User user = getUserById(userId).orElseThrow(UserNotFoundException::new);
        user.editInfo(name, surname, description);
        updateUserGenres(genresNames, user);
        updateUserRoles(rolesNames, user);
        updateProfilePicture(user,image);
    }

    @Override
    public Set<Role> getUserRoles(User user) {
        return user.getUserRoles();
    }

    @Transactional
    @Override
    public void updateUserRoles(List<String> rolesNames, User user) {
        Set<Role> roles = roleService.getRolesByNames(rolesNames);
        if(roles.size() > MAX_USER_ROLES)
            throw new IllegalArgumentException();
        user.setUserRoles(roles);
    }

    @Override
    public Set<Genre> getUserGenres(User user) {
        return user.getUserGenres();
    }

    @Override
    public Set<SocialMedia> getUserSocialMedia(User user) {
        return user.getSocialSocialMedia();
    }

    @Transactional
    @Override
    public void updateUserGenres(List<String> genreNames, User user) {
        Set<Genre> genres = genreService.getGenresByNames(genreNames);
        if(genres.size() > MAX_USER_GENRES)
            throw new IllegalArgumentException();
        user.setUserGenres(genres);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Transactional
    @Override
    public void verifyUser(String token) {
        long userId = verificationTokenService.getTokenOwner(token, TokenType.VERIFY);
        userDao.verifyUser(userId);
        autoLogin(userId);
    }

    private void autoLogin(long userId) {
        final User user = getUserById(userId).orElseThrow(UserNotFoundException::new);
        final Collection<GrantedAuthority> authorities = new ArrayList<>();
        if(user.isBand())
            authorities.add(new SimpleGrantedAuthority("ROLE_BAND"));
        else
            authorities.add(new SimpleGrantedAuthority("ROLE_ARTIST"));
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(),user.getPassword(),authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        LOGGER.debug("Autologin for user {}",userId);
    }

    @Transactional
    @Override
    public void sendResetEmail(String email) {
        User user = userDao.findByEmail(email).orElseThrow(UserNotFoundException::new);
        verificationTokenService.deleteTokenByUserId(user.getId(), TokenType.RESET);
        VerificationToken token = verificationTokenService.generate(user, TokenType.RESET);
        Locale locale = LocaleContextHolder.getLocale();
        LocaleContextHolder.setLocale(locale, true);
        mailingService.sendResetPasswordEmail(user, token, locale);
    }

    @Transactional
    @Override
    public void changePassword(String token, String newPassword) {
        long userId = verificationTokenService.getTokenOwner(token, TokenType.RESET);
        userDao.changePassword(userId, passwordEncoder.encode(newPassword));
        autoLogin(userId);
    }

    @Override
    public byte[] getProfilePicture(long userId) throws IOException {
        User user = getUserById(userId).orElseThrow(UserNotFoundException::new);
        byte[] image = user.getProfileImage();
        if(image == null) {
            File file;
            if(user.isBand())
                file = ResourceUtils.getFile("classpath:images/band.jpg");
            else
                file = ResourceUtils.getFile("classpath:images/artist.png");
            InputStream fileStream = new FileInputStream(file);
            return IOUtils.toByteArray(Objects.requireNonNull(fileStream));
        }
        return image;
    }

    @Transactional
    @Override
    public void updateSocialMedia(User user, Set<MediaUrl> mediaUrls) {
        Set<SocialMedia> socialMedia = mediaUrls.stream().map(mediaUrl -> new SocialMedia(user,mediaUrl.getUrl(),mediaUrl.getType())).collect(Collectors.toSet());
        user.setSocialSocialMedia(socialMedia);
    }

    @Transactional
    @Override
    public void updateProfilePicture(User user, byte[] image) {
        if(image.length > 0)
            user.setProfileImage(image);
    }

    @Override
    public List<User> filter(FilterOptions filter, int page) {
        int lastPage = getFilterTotalPages(filter);
        lastPage = lastPage == 0 ? 1 : lastPage;
        checkPage(page, lastPage);
        return userDao.filter(filter, page);
    }

    @Override
    public int getFilterTotalPages(FilterOptions filter) {
        return userDao.getTotalPages(filter);
    }

    private void checkPage(int page, int lastPage) {
        if(page <= 0)
            throw new IllegalArgumentException();
        if(page > lastPage)
            throw new PageNotFoundException();
    }

}
