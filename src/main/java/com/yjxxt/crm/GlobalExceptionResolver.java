package com.yjxxt.crm;

import com.alibaba.fastjson.JSON;
import com.yjxxt.crm.base.ResultInfo;
import com.yjxxt.crm.exceptions.NoAuthException;
import com.yjxxt.crm.exceptions.NoLoginException;
import com.yjxxt.crm.exceptions.ParamsException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class GlobalExceptionResolver implements HandlerExceptionResolver {
    /**
     * 试图名称
     * json数据
     *
     * @param req
     * @param resp
     * @param handler
     * @param ex
     * @return
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex) {
        //未登录异常
        if(ex instanceof NoLoginException){
            ModelAndView mav=new ModelAndView("redirect:/index");
            return  mav;
        }
        //实例化对象
        ModelAndView mav=new ModelAndView("error");
        //存储数据
        mav.addObject("code", 400);
        mav.addObject("msg", "系统异常，请稍后再试...");
        //判断
        if(handler instanceof HandlerMethod){
            //
            HandlerMethod handlerMethod=(HandlerMethod) handler;
            ResponseBody responseBody = handlerMethod.getMethod().getDeclaredAnnotation(ResponseBody.class);
            //判断是否有responseBody
            if(responseBody==null){
                //返回视图名称
                if(ex instanceof ParamsException){
                    ParamsException pe=(ParamsException)ex;
                    mav.addObject("code",pe.getCode());
                    mav.addObject("msg",pe.getMsg());
                }
                return  mav;
            }else{
                //返回json  resultInfo
                ResultInfo resultInfo=new ResultInfo();
                resultInfo.setCode(300);
                resultInfo.setMsg("参数异常了");
                //
                if(ex instanceof ParamsException){
                    ParamsException pe=(ParamsException)ex;
                    resultInfo.setCode(pe.getCode());
                    resultInfo.setMsg(pe.getMsg());
                }

                if(ex instanceof NoAuthException){
                    NoAuthException pe=(NoAuthException)ex;
                    resultInfo.setCode(pe.getCode());
                    resultInfo.setMsg(pe.getMsg());
                }
                //响应resultInfo
                resp.setContentType("application/json;charset=utf-8");
                PrintWriter pw = null;
                try {
                    pw = resp.getWriter();
                    //resultInfo--json
                    pw.write(JSON.toJSONString(resultInfo));
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if(pw!=null){
                        pw.close();
                    }
                }
                return  null;

            }
        }
        return mav;
    }
}
