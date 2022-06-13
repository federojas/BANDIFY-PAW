package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Application;
import ar.edu.itba.paw.model.ApplicationState;
import ar.edu.itba.paw.model.User;

import java.util.List;
import java.util.Optional;

public interface ApplicationService {

    List<Application> getAuditionApplicationsByState(long auditionId, ApplicationState state, int page);

    List<Application> getAuditionApplicationsByState(long auditionId, ApplicationState state);

    boolean apply(long auditionId, User user, String message);

    void accept(long auditionId, long applicantId);

    void reject(long auditionId, long applicantId);

    void select(long auditionId, long applicantId);

    List<Application> getMyApplications(long applicantId, int page);

    int getTotalUserApplicationPages(long userId);

    int getTotalUserApplicationPagesFiltered(long userId, ApplicationState state);

    List<Application> getMyApplicationsFiltered(long applicantId, int page, ApplicationState state);

    int getTotalAuditionApplicationByStatePages(long auditionId, ApplicationState state);

    boolean alreadyApplied(long auditionId, long applicantId);

    Optional<Application> getApplicationById(long auditionId, long applicationId) ;

    Optional<Application> getAcceptedApplicationById(long auditionId, long applicationId);

    void closeApplicationsByAuditionId(long id);

    int getTotalUserApplicationsFiltered(long userId, ApplicationState state);
}
