// import { axiosPrivate } from "../api";
import { axiosPrivate } from "../api/api";
import { useEffect } from "react";
import useAuth from "./useAuth";

const useAxiosPrivate = () => {
  const auth = useAuth();

  const requestIntercept = axiosPrivate.interceptors.request.use(
    config => {
      if (auth?.isAuthenticated && config.headers && !(config.headers as any)['Authorization']) {
        (config.headers as any)['Authorization'] = `Bearer ${auth?.jwt}`;
      }

      return config;
    }, (error) => Promise.reject(error)
  );

  const responseIntercept = axiosPrivate.interceptors.response.use(
    response => response,
    async (error) => {
      const prevRequest = error?.config;
      // TODO: el 401 puede venir de Bad Credentials cuando la request es de login, hay que revisar eso.
      if (error?.response?.status === 401 && !prevRequest?.sent) {
        prevRequest.sent = true;
        // TODO: ver porque aca usar auth?.refreshToken no funciona bien
        prevRequest.headers['Authorization'] = `Bearer ${localStorage.getItem("refresh")}`;

        const response = axiosPrivate(prevRequest).then((response) => {
          if (response.headers && response.headers["x-jwt"])
          auth?.login(response.headers["x-jwt"]);
          return response;
        }).catch((error) => {
          if (error?.response?.status === 401) {
            auth?.logout();
          }
          throw error;
        });
        return response;
      }
      return Promise.reject(error);
    }
  );

  return axiosPrivate;
}

export default useAxiosPrivate;