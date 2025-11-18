const obj = Object.create(Constructor.prototype);
const result = Constructor.apply (obj,arg);
return typeof result === 'object' && result !== null ? result : obj;


//手写题
function myInstance(left,right){
   if(typeof left !== 'object' || left === null) return false;
   let proto = left.__proto__;
   const prototype = right.prototype;
   while(proto){
    if(proto === prototype) return true;
    proto = proto.__proto__;
   }
   return false;
}

function debounce (fn, delay){
    let  timer = null;
    return function(...args){

        clearTimeout(timer);//clara the last one 
        timer= setTimeout(()=>{
            fn.apply(this ,args);
        },delay )//set the new one ,remian this and the arguments

    }
}


function debxxx(fn,delay){
let timer = null;
return function(...args){
     clearTimeout(timer);
     timer= setTimeout(()=>{
fn.apply(this,args);
     },delay)
}
}

function throttle (fn,delay){
    let last =0;
     return function(...args){
        const now = Date.now();
        if(now-last>=delay){
            last =now ;
            fn.apply(this,args );
           
        }
     }
}


function thxx(fn ,delay)
{
     let timer = null;
     return function(...args){
        if(!timer){
             timer = setTimeout(()=>{
                fn.apply(this ,args);
                timer= null ;

             },delay)
        }
     }
}
//每隔一段时间就出发一次

function myisntance (left ,right){
    if (typeof left !='object'||left ===null)
 return false ;
let proto = left.__proto__;
const prototype = right.prototype;
while(proto){
    if (proto === prototype) return true;
    proto = proto.__proto__;
}
return false;
}

function myisxx (left,right){
    if(typeof left != 'object'|| left ===null){
        return false;
        
    }
    let proto = left.__proto__;
    const prototype = right.prototype;
    while (proto){
        if (proto=== prototype)return true;
        proto= proto.__proto__;

    }
return false ;

}

//flat
function flat(arr){
    const result = [];
    for(let item of arr){
        if(Array.isArray(item)){
            result.push(...flat(item));
        }else{
            result.push(item);
        }
    }
    return result;
}


function curry(fn){
    return function curried (...args ){

        if (args.length >= fn.length){
            return fn.apply(this,args);
    }else {
        return function curried (...args2){
            return curried.apply(this,[arg,...args2]);
        }
    }
}}


//千分位隔开
function formatthousand (num ){
    return num.toString().replace(/\B(\d{3}+(?!\d))/g,',');
}
function xx(num ){
    const str = num.toString();
    let result = ' ';
    let count = 0;
    for (let i = str.length-1; i>=0;i--){
        result = result+str[i];
count++;
if(count%3===0&& i!=0){
    result= ','+result;

}
    }
}


function promiseall(promises){
     return new Promises ((resolve,reject)=>{
        const result=[];
    let count = 0;
 

promises.forEach((p,i) => {
    Promise.resolve(p).then(res=>{
        result[i]=res ;
        count++;
        if(count === promises.length){
            resolve(result);

        }
    })
    
});})
}

function deepclone (obj ){
    if(obj ===null || typeof obj != 'object') return obj;
    const result = Array.isArray(obj )?[]:{};
    for (let key in obj ){
        if (obj.hasOwnProperty(key)){
            result[key] = deepclone(obj[key]);


        }
    }
    return result;
}


//shenkaobei 
function shenxxx(obj){
    if (obj ===null || typeof obj !='object') return obj ;
    const result = Array.isArray(obj )?[]:{};
     for (let things in obj ){
        if (obj.hasOwnProperty
            (things)
        ){
            result[things]= deepclone(obj[things]);

        }
     }
     return result;
}