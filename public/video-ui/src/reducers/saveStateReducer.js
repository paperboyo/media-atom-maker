import { saveStateVals } from '../constants/saveStateVals';

export default function saveState(
  state = {
    saving: false,
    publishing: false
  },
  action
) {
  switch (action.type) {
    //Save States
    case 'VIDEO_GET_REQUEST':
      return Object.assign({}, state, {
        saving: saveStateVals.inprogress
      });
    case 'VIDEO_CREATE_REQUEST':
      return Object.assign({}, state, {
        saving: saveStateVals.inprogress
      });
    case 'ASSET_REVERT_REQUEST':
      return Object.assign({}, state, {
        saving: saveStateVals.inprogress
      });
    case 'VIDEO_SAVE_REQUEST':
      return Object.assign({}, state, {
        saving: saveStateVals.inprogress
      });
    case 'VIDEO_GET_RECEIVE':
      return Object.assign({}, state, {
        saving: false
      });

    case 'VIDEO_CREATE_RECEIVE':
      return Object.assign({}, state, {
        saving: false
      });
    case 'ASSET_REVERT_RECEIVE':
      return Object.assign({}, state, {
        saving: false
      });
    case 'VIDEO_SAVE_RECEIVE':
      return Object.assign({}, state, {
        saving: false
      });
    //Publish States


    case 'VIDEO_PUBLISH_REQUEST':
      return Object.assign({}, state, {
        publishing: saveStateVals.inprogress
      });
    case 'VIDEO_PUBLISH_RECEIVE':
      return Object.assign({}, state, {
        publishing: false
      });
    // Adding asset states


    case 'ASSET_CREATE_REQUEST':
      return Object.assign({}, state, {
        addingAsset: saveStateVals.inprogress
      });
    case 'ASSET_CREATE_RECEIVE':
      return Object.assign({}, state, {
        addingAsset: false
      });
    // Checkign usages state
    case 'VIDEO_USAGE_GET_REQUEST':
      return Object.assign({}, state, {
        fetchingUsages: true
      });
    case 'VIDEO_USAGE_GET_RECEIVE':
      return Object.assign({}, state, {
        fetchingUsages: false
      });

    case 'SHOW_ERROR':
      return Object.assign({}, state, {
        saving: false,
        publishing: false
      });

    default:
      return state;
  }
}
