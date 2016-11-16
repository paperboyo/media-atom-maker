import {pandaReqwest} from './pandaReqwest';


export default {

  fetchVideos: () => {
    return pandaReqwest({
      url: '/api/atoms',
      method: 'get',
      contentType: 'application/json',
    });
  },

  fetchVideo: (videoId) => {
    return pandaReqwest({
      url: '/api2/atom/' + videoId,
      method: 'get',
      contentType: 'application/json',
    });
  },

  createVideo: (video) => {
    return pandaReqwest({
      url: '/api/atom',
      method: 'post',
      data: video.data
    })
  },

  publishVideo: (videoId) => {
    return pandaReqwest({
      url: '/api/atom/' + videoId + '/publish',
      method: 'put'
    })
  },

  createAsset: (asset, videoId) => {
    return pandaReqwest({
      url: '/api2/atom/' + videoId + '/asset',
      method: 'put',
      contentType: 'application/json',
      data: asset
    })
  },

  revertAsset: (version, videoId) => {
    return pandaReqwest({
      url: '/api/atom/' + videoId + '/revert/' + version,
      method: 'put'
    })
  },

  saveVideo: (videoId, video) => {
    return pandaReqwest({
      url: '/api/atom/' + videoId,
      method: 'put',
      data: video.data
    })
  }

}
