/*
 * Default text - jQuery plugin for html5 dragging files from desktop to browser
 * Author: Weixi Yen
 * Copyright (c) 2010 Resopollution
 * Licensed under the MIT license:
 *   http://www.opensource.org/licenses/mit-license.php
 * Project home:
 *   http://www.github.com/weixiyen/jquery-filedrop
 * Version:  0.1.0
 */
function(e){function u(){}var t={fallback_id:"",url:"",refresh:1e3,paramname:"userfile",requestType:"POST",allowedfiletypes:[],maxfiles:25,maxfilesize:1,queuefiles:0,queuewait:200,data:{},headers:{},drop:u,dragStart:u,dragEnter:u,dragOver:u,dragLeave:u,docEnter:u,docOver:u,docLeave:u,beforeEach:u,afterAll:u,rename:u,error:function(e,t,n,r){alert(e)},uploadStarted:u,uploadFinished:u,progressUpdated:u,globalProgressUpdated:u,speedUpdated:u},n=["BrowserNotSupported","TooManyFiles","FileTooLarge","FileTypeNotAllowed","NotFound","NotReadable","AbortError","ReadError"],r,i=false,s=0,o;e.fn.filedrop=function(u){function l(e){e.preventDefault();e.stopPropagation();if(a.drop.call(this,e)===false)return false;o=e.originalEvent.dataTransfer.files;if(o===null||o===undefined||o.length===0){a.error(n[0]);return false}s=o.length;d();e.preventDefault();return false}function c(t,n,r,i){var s="--",o="\r\n",u="";if(a.data){var f=e.param(a.data).replace(/\+/g,"%20").split(/&/);e.each(f,function(){var e=this.split("=",2),t=decodeURIComponent(e[0]),n=decodeURIComponent(e[1]);if(e.length!==2){return}u+=s;u+=i;u+=o;u+='Content-Disposition: form-data; name="'+t+'"';u+=o;u+=o;u+=n;u+=o})}u+=s;u+=i;u+=o;u+='Content-Disposition: form-data; name="'+a.paramname+'"';u+='; filename="'+encodeURIComponent(t)+'"';u+="; filename*=UTF-8''" + encodeURIComponent(t);u+=o;u+="Content-Type: "+r;u+=o;u+=o;u+=n;u+=o;u+=s;u+=i;u+=s;u+=o;return u}function h(e){if(e.lengthComputable){var t=Math.round(e.loaded*100/e.total);if(this.currentProgress!==t){this.currentProgress=t;a.progressUpdated(this.index,this.file,this.currentProgress);f[this.global_progress_index]=this.currentProgress;p();var n=(new Date).getTime();var r=n-this.currentStart;if(r>=a.refresh){var i=e.loaded-this.startData;var s=i/r;a.speedUpdated(this.index,this.file,s);this.startData=e.loaded;this.currentStart=n}}}}function p(){if(f.length===0){return}var e=0,t;for(t in f){if(f.hasOwnProperty(t)){e=e+f[t]}}a.globalProgressUpdated(Math.round(e/f.length))}function d(){i=false;if(!o){a.error(n[0]);return false}if(a.allowedfiletypes.push&&a.allowedfiletypes.length){for(var t=o.length;t--;){if(!o[t].type||e.inArray(o[t].type,a.allowedfiletypes)<0){a.error(n[3],o[t]);return false}}}var r=0,u=0;if(s>a.maxfiles&&a.queuefiles===0){a.error(n[1]);return false}var l=[];var d=[];var b=[];for(var w=0;w<s;w++){l.push(w)}var E=function(e){setTimeout(S,e);return};var S=function(){var e;if(i){return false}if(a.queuefiles>0&&d.length>=a.queuefiles){return E(a.queuewait)}else{e=l[0];l.splice(0,1);d.push(e)}try{if(g(o[e])!==false){if(e===s){return}var t=new FileReader,r=1048576*a.maxfilesize;t.index=e;if(o[e].size>r){a.error(n[2],o[e],e);d.forEach(function(t,n){if(t===e){d.splice(n,1)}});u++;return true}t.onerror=function(e){switch(e.target.error.code){case e.target.error.NOT_FOUND_ERR:a.error(n[4]);return false;case e.target.error.NOT_READABLE_ERR:a.error(n[5]);return false;case e.target.error.ABORT_ERR:a.error(n[6]);return false;default:a.error(n[7]);return false}};t.onloadend=!a.beforeSend?x:function(t){a.beforeSend(o[e],e,function(){x(t)});x(t)};t.readAsDataURL(o[e])}else{u++}}catch(f){d.forEach(function(t,n){if(t===e){d.splice(n,1)}});a.error(n[0]);return false}if(l.length>0){S()}};var x=function(t){var n=(t.srcElement||t.target).index;if(t.target.index===undefined){t.target.index=v(t.total)}var l=new XMLHttpRequest,g=l.upload,w=o[t.target.index],E=t.target.index,S=(new Date).getTime(),x="------multipartformboundary"+(new Date).getTime(),T=f.length,N,C=m(w.name),k=w.type;if(a.withCredentials){l.withCredentials=a.withCredentials}var L=atob(t.target.result.split(",")[1]);if(typeof C==="string"){N=c(C,L,k,x)}else{N=c(w.name,L,k,x)}g.index=E;g.file=w;g.downloadStartTime=S;g.currentStart=S;g.currentProgress=0;g.global_progress_index=T;g.startData=0;g.addEventListener("progress",h,false);if(e.isFunction(a.url)){l.open(a.requestType,a.url(),true)}else{l.open(a.requestType,a.url,true)}l.setRequestHeader("content-type","multipart/form-data; boundary="+x);l.setRequestHeader("X-Requested-With","XMLHttpRequest");e.each(a.headers,function(e,t){l.setRequestHeader(e,t)});l.sendAsBinary(N);f[T]=0;p();a.uploadStarted(E,w,s);l.onload=function(){var t=null;if(l.responseText){try{t=e.parseJSON(l.responseText)}catch(o){t=l.responseText}}var c=(new Date).getTime(),h=c-S,v=a.uploadFinished(E,w,t,h,l);r++;d.forEach(function(e,t){if(e===n){d.splice(t,1)}});b.push(n);f[T]=100;p();if(r===s-u){y()}if(v===false){i=true}if(l.status<200||l.status>299){a.error(l.statusText,w,n,l.status)}}};S()}function v(e){for(var t=0;t<s;t++){if(o[t].size===e){return t}}return undefined}function m(e){return a.rename(e)}function g(e){return a.beforeEach(e)}function y(){return a.afterAll()}function b(e){e.preventDefault();e.stopPropagation();clearTimeout(r);e.preventDefault();a.dragEnter.call(this,e)}function w(e){e.preventDefault();e.stopPropagation();clearTimeout(r);e.preventDefault();a.docOver.call(this,e);a.dragOver.call(this,e)}function E(e){e.preventDefault();e.stopPropagation();clearTimeout(r);a.dragLeave.call(this,e);e.stopPropagation()}function S(e){e.preventDefault();e.stopPropagation();a.docLeave.call(this,e);return false}function x(e){e.preventDefault();e.stopPropagation();clearTimeout(r);e.preventDefault();a.docEnter.call(this,e);return false}function T(e){e.preventDefault();e.stopPropagation();clearTimeout(r);e.preventDefault();a.docOver.call(this,e);return false}function N(e){e.preventDefault();e.stopPropagation();r=setTimeout(function(t){return function(){a.docLeave.call(t,e)}}(this),200)}var a=e.extend({},t,u),f=[];this.on("drop",l).on("dragstart",a.dragStart).on("dragenter",b).on("dragover",w).on("dragleave",E);e(document).on("drop",S).on("dragenter",x).on("dragover",T).on("dragleave",N);e("#"+a.fallback_id).change(function(e){a.drop(e);o=e.target.files;s=o.length;d()});return this};try{if(XMLHttpRequest.prototype.sendAsBinary){return}XMLHttpRequest.prototype.sendAsBinary=function(e){function t(e){return e.charCodeAt(0)&255}var n=Array.prototype.map.call(e,t);var r=new Uint8Array(n);this.send(r.buffer)}}catch(a){}
}($)