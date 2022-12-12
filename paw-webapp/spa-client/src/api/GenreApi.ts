import api from './api';
import Genre from './types/Genre'

const GenreApi = (() => {
    const endpoint = '/genres';

    interface Params {
        auditionId?: number;
        userId?: number;
    }

    const getGenres = async (params : Params | undefined) => {
        if (!params) {
            return api.get(endpoint);
        }

        return api.get(endpoint, {params: {
            audition: params.auditionId, 
            user: params.userId
        }});
    }

    const getGenreById = (id: number) => {
        return api.get(`${endpoint}/${id}`);
    }

    return {
        getGenres,
        getGenreById
    }
}
)();

export default GenreApi;