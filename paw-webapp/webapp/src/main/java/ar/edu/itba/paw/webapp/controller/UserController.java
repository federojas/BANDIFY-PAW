package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.AuditionService;
import ar.edu.itba.paw.service.UserService;
import ar.edu.itba.paw.service.VerificationTokenService;
import ar.edu.itba.paw.webapp.form.NewPasswordForm;
import ar.edu.itba.paw.webapp.form.ResetPasswordForm;
import ar.edu.itba.paw.webapp.form.UserArtistForm;
import ar.edu.itba.paw.webapp.form.UserBandForm;
import ar.edu.itba.paw.webapp.security.services.SecurityFacade;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class UserController {

    private final UserService userService;
    private final SecurityFacade securityFacade;
    private final VerificationTokenService verificationTokenService;
    private final AuditionService auditionService;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService, SecurityFacade securityFacade,
                          VerificationTokenService verificationTokenService,
                          AuditionService auditionService) {
        this.userService = userService;
        this.securityFacade = securityFacade;
        this.verificationTokenService = verificationTokenService;
        this.auditionService = auditionService;
    }

    @RequestMapping(value = {"/register","/registerBand", "/registerArtist"},
            method = {RequestMethod.GET})
    public ModelAndView registerView(@ModelAttribute("userBandForm") final UserBandForm userBandForm,
                                     @ModelAttribute("userArtistForm") final UserArtistForm userArtistForm,
                                     boolean isBand) {
        ModelAndView mav = new ModelAndView("views/register");
        mav.addObject("isBand", isBand);
        mav.addObject("userEmailAlreadyExists", false);
        return mav;
    }

    //TODO: MODULARIZAR CODIGO REPETIDO EN REGISTERS
    @RequestMapping(value = "/registerBand", method = {RequestMethod.POST})
    public ModelAndView registerBand(@Valid @ModelAttribute("userBandForm") final UserBandForm userBandForm,
                                     final BindingResult errors) {
        if (errors.hasErrors()) {
            ModelAndView returnRegister = registerView(userBandForm, new UserArtistForm(), true);
            returnRegister.addObject("userArtistForm", new UserArtistForm());
            return returnRegister;
        }

        User.UserBuilder user = new User.UserBuilder(userBandForm.getEmail(), userBandForm.getPassword(),
                userBandForm.getName(), userBandForm.isBand(), false);

        userService.create(user);

        return emailSent(userBandForm.getEmail());
    }

    //TODO: MODULARIZAR CODIGO REPETIDO EN REGISTERS
    @RequestMapping(value = "/registerArtist", method = {RequestMethod.POST})
    public ModelAndView registerArtist(@Valid @ModelAttribute("userArtistForm") final UserArtistForm userArtistForm,
                                       final BindingResult errors) {
        if (errors.hasErrors()) {
            ModelAndView returnRegister = registerView(new UserBandForm(), userArtistForm, false);
            returnRegister.addObject("userBandForm", new UserBandForm());
            return returnRegister;
        }

        User.UserBuilder user = new User.UserBuilder(userArtistForm.getEmail(), userArtistForm.getPassword(),
                userArtistForm.getName(), userArtistForm.isBand(), false)
                .surname(userArtistForm.getSurname());

        userService.create(user);

        return emailSent(userArtistForm.getEmail());
    }


    @RequestMapping(value = "/profile", method = {RequestMethod.GET})
    public ModelAndView profile() {
        ModelAndView mav = new ModelAndView("views/profile");
        User user = securityFacade.getCurrentUser();
        mav.addObject("user", user);
        if(user.isBand())
            mav.addObject("auditions", auditionService.getBandAuditions(securityFacade.getCurrentUser().getId()));
        return mav;
    }

    @RequestMapping(value = "/verify")
    public ModelAndView verify(@RequestParam(required = true) final String token) {
        userService.verifyUser(token);
        return new ModelAndView("views/verified");
    }

    @RequestMapping(value = "/resetPassword", method = {RequestMethod.GET})
    public ModelAndView resetPassword(@ModelAttribute("resetPasswordForm")
                                      final ResetPasswordForm resetPasswordForm) {

        ModelAndView mav = new ModelAndView("/views/resetPassword");
        mav.addObject("emailNotFound", false);
        mav.addObject("emailSent", false);
        return mav;
    }

    @RequestMapping(value = "/resetPassword", method = {RequestMethod.POST})
    public ModelAndView resetPassword(@Valid @ModelAttribute("resetPasswordForm")
                                      final ResetPasswordForm resetPasswordForm,
                                      final BindingResult errors) {
        if (errors.hasErrors()) {
           return resetPassword(resetPasswordForm);
        }
        userService.sendResetEmail(resetPasswordForm.getEmail());
        ModelAndView mav = new ModelAndView("/views/resetPassword");
        mav.addObject("resetPasswordForm",resetPasswordForm);
        mav.addObject("emailNotFound", false);
        mav.addObject("emailSent", true);
        return mav;
    }

    @RequestMapping(value = "/newPassword", method = {RequestMethod.GET})
    public ModelAndView newPassword(@RequestParam(required = true) final String token,
                                    @ModelAttribute("newPasswordForm") final NewPasswordForm newPasswordForm) {
        if(verificationTokenService.isValid(token)) {
            ModelAndView mav = new ModelAndView("/views/newPassword");
            mav.addObject("token",token);
            return mav;
        }

        return new ModelAndView("redirect:/errors/404");
    }

    @RequestMapping(value = "/newPassword", method = {RequestMethod.POST})
    public ModelAndView newPassword(@RequestParam(required = true) final String token,
                                    @Valid @ModelAttribute("newPasswordForm") final NewPasswordForm newPasswordForm,
                                    final BindingResult errors) {

        if (errors.hasErrors()) {
            return newPassword(token, newPasswordForm);
        }

        userService.changePassword(token, newPasswordForm.getNewPassword());

        return new ModelAndView("redirect:/welcome");
    }

    @RequestMapping(value = "/emailSent", method = {RequestMethod.GET})
    public ModelAndView emailSent(String email) {
        ModelAndView emailSent = new ModelAndView("/views/emailSent");
        emailSent.addObject("email", email);
        return emailSent;
    }

    @RequestMapping(value = "/emailSent", method = {RequestMethod.POST})
    public ModelAndView resendEmail(@RequestParam final String email) {
        userService.resendUserVerification(email);
        return emailSent(email);
    }

    @RequestMapping(value = "/login", method = {RequestMethod.GET})
    public ModelAndView login() {
        return new ModelAndView("views/login");
    }
}
