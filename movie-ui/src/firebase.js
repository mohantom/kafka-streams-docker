import firebase from "firebase";
// import firestore from 'firebase/firestore'

// Initialize Firebase
var firebaseConfig = {
  apiKey: "AIzaSyCmUAOOE8DDCsslWZCUF_zI6m7Ogfnp9fA",
  authDomain: "vue-demo-7bbda.firebaseapp.com",
  databaseURL: "https://vue-demo-7bbda.firebaseio.com",
  projectId: "vue-demo-7bbda",
  storageBucket: "vue-demo-7bbda.appspot.com",
  messagingSenderId: "1047652131794",
  appId: "1:1047652131794:web:4fb9172df2bc83676b5e0b"
};
firebase.initializeApp(firebaseConfig);

const db = firebase.firestore();
const auth = firebase.auth();

export { auth, db };
